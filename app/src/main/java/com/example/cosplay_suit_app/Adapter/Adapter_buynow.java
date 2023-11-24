package com.example.cosplay_suit_app.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cosplay_suit_app.API;
import com.example.cosplay_suit_app.DTO.CartOrderDTO;
import com.example.cosplay_suit_app.DTO.DTO_CartOrder;
import com.example.cosplay_suit_app.DTO.DTO_buynow;
import com.example.cosplay_suit_app.DTO.DTO_inbuynow;
import com.example.cosplay_suit_app.DTO.ShopCartorderDTO;
import com.example.cosplay_suit_app.DTO.TotalPriceManager;
import com.example.cosplay_suit_app.Interface_retrofit.CartOrderInterface;
import com.example.cosplay_suit_app.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Adapter_buynow extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    static String url = API.URL;
    static final String BASE_URL = url +"/bill/";
    List<DTO_buynow> list;
    Context context;
    private Map<String, List<DTO_inbuynow>> orderMap; // Map lưu trữ danh sách đơn hàng theo idshop
    private List<DTO_inbuynow> allOrders;
    Adapter_inbuynow arrayAdapter;
    String TAG = "adaptershopcartorder";
    private TotalPriceManager totalPriceManager;

    public Adapter_buynow(List<DTO_buynow> list, Context context) {
        this.list = list;
        this.context = context;
        orderMap = new HashMap<>();
        allOrders = new ArrayList<>();
        totalPriceManager = TotalPriceManager.getInstance();
        ArrayList<String> listStringIDcart = totalPriceManager.getListcart();
        // Lấy danh sách đơn hàng của tất cả người dùng
        SharedPreferences sharedPreferences = context.getSharedPreferences("User", context.MODE_PRIVATE);
        String id = sharedPreferences.getString("id","");
        getOrdersByUserId(id, new Callback<List<DTO_inbuynow>>() {
            @Override
            public void onResponse(Call<List<DTO_inbuynow>> call, Response<List<DTO_inbuynow>> response) {
                if (response.isSuccessful()) {
                    allOrders = response.body();
                    for (DTO_inbuynow order : allOrders) {
                        String checkorder = order.get_id();
                        if (listStringIDcart.contains(checkorder)) {
                            handleOrders(order.getDtoSanPham().getId_shop(), order);
                        }
                    }
                    notifyDataSetChanged();
                } else {
                    // Xử lý lỗi (nếu có)
                    Log.e("Adapter_ShopCartOrder", "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<DTO_inbuynow>> call, Throwable t) {
                // Xử lý lỗi khi gọi API
                Log.e("Adapter_ShopCartOrder", "Error: " + t.getMessage());
            }
        });
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_buynow, parent, false);
        return new ItemViewHoldel(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DTO_buynow shopCartorderDTO = list.get(position);

        Adapter_buynow.ItemViewHoldel viewHolder = (Adapter_buynow.ItemViewHoldel) holder;

        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");

        List<DTO_inbuynow> ordersForShop = orderMap.get(shopCartorderDTO.get_id());
        arrayAdapter = new Adapter_inbuynow(ordersForShop, context);
        viewHolder.recyclerView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();

        // Tính tổng và hiển thị
        int totalForShop = calculateTotalForShop(ordersForShop);
        viewHolder.tv_tonggia.setText( decimalFormat.format(totalForShop) + " VND");

        //Chọn phương thức thanh toán
        viewHolder.idchonphuongthuc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogchonthanhtoan(viewHolder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ItemViewHoldel extends RecyclerView.ViewHolder{
        RecyclerView recyclerView;
        TextView tv_tonggia, idchonphuongthuc;
        public ItemViewHoldel(@NonNull View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.rcv_buynow);
            tv_tonggia = itemView.findViewById(R.id.tv_tonggia);
            idchonphuongthuc = itemView.findViewById(R.id.idchonphuongthuc);
        }
    }
    public void getOrdersByUserId(String userId, Callback<List<DTO_inbuynow>> callback) {
        // Tạo một OkHttpClient với interceptor để ghi log (nếu cần)
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(logging);

        // Tạo Gson ConverterFactory để chuyển đổi JSON thành Java objects
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();

        // Tạo một API service sử dụng Retrofit
        CartOrderInterface service = retrofit.create(CartOrderInterface.class);

        // Gọi API để lấy danh sách đơn hàng dựa trên userId
        Call<List<DTO_inbuynow>> call = service.getShopidcart(userId);
        call.enqueue(callback);
    }
    private void handleOrders(String idShop, DTO_inbuynow order) {
        if (orderMap.containsKey(idShop)) {
            orderMap.get(idShop).add(order);
        } else {
            List<DTO_inbuynow> orders = new ArrayList<>();
            orders.add(order);
            orderMap.put(idShop, orders);
        }
    }
    private int calculateTotalForShop(List<DTO_inbuynow> orders) {
        int total = 0;
        for (DTO_inbuynow order : orders) {
            total += order.getAmount() * order.getDtoSanPham().getPrice();
        }
        return total;
    }
    public void dialogchonthanhtoan(ItemViewHoldel viewHoldel){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_chonthanhtoan);

        //thoát khỏi dialog
        ImageView icback = dialog.findViewById(R.id.id_back);
        icback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        //Xử lý công việc trong radiogroup
        RadioGroup radioGroup = dialog.findViewById(R.id.rdo_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                // Lấy RadioButton đã chọn
                RadioButton selectedRadioButton = dialog.findViewById(checkedId);

                // Lấy ID của RadioButton đã chọn
                int selectedId = radioGroup.getCheckedRadioButtonId();

                // Xử lý công việc dựa trên ID của RadioButton
                if (selectedId == R.id.rdo_thanhtoansau) {
                    // Xử lý công việc rdo_thanhtoansau
                    viewHoldel.idchonphuongthuc.setText("Thanh toán khi nhận hàng");
                } else if (selectedId == R.id.rdo_thanhtoanVnpay) {
                    // Xử lý công việc rdo_thanhtoanVnpay
                    Toast.makeText(context, "rdo_thanhtoanVnpay", Toast.LENGTH_SHORT).show();
                    viewHoldel.idchonphuongthuc.setText("Thanh toán VNpay");
                }
            }
        });

        Window window = dialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();

        // Chiều rộng full màn hình
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        // Chiều cao full màn hình
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        window.setAttributes(layoutParams);
        window.setBackgroundDrawableResource(android.R.color.transparent);

        dialog.show();
    }



}
