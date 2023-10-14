package com.example.cosplay_suit_app.Package_bill;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.example.cosplay_suit_app.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class Collection_adapter_bill extends AppCompatActivity {
    ViewPager2 viewPager2;
    // adapter
    PagerAdapter pagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conllection_pager_bill);
        pagerAdapter = new PagerAdapter(Collection_adapter_bill.this);
        viewPager2 = findViewById(R.id.pager2);
        viewPager2.setAdapter( pagerAdapter );

        /// làm việc với Tab
        TabLayout tabLayout01 = findViewById(R.id.idtablayout);
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout01, viewPager2,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        // thiết lập tiêu đề tab
                        tab.setText( "Tab thứ ... " + position );

                        // muốn tùy chỉnh riêng cho từng tab: thì dùng swich case để kiểm tra postion và tùy chỉnh.
                    }
                });

        mediator.attach();
    }
}