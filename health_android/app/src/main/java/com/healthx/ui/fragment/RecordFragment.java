package com.healthx.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.healthx.R;
import com.healthx.model.HealthCard;
import com.healthx.ui.adapter.HealthCardAdapter;

import java.util.ArrayList;
import java.util.List;

public class RecordFragment extends Fragment {

    private RecyclerView recyclerView;
    private HealthCardAdapter adapter;
    private FloatingActionButton fabAddRecord;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
        setupListeners();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_health_cards);
        fabAddRecord = view.findViewById(R.id.fab_add_record);
    }

    private void setupRecyclerView() {
        adapter = new HealthCardAdapter(getHealthCards());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAddRecord.setOnClickListener(v -> 
            Toast.makeText(requireContext(), "添加记录", Toast.LENGTH_SHORT).show()
        );
    }

    private List<HealthCard> getHealthCards() {
        // 模拟数据，实际应从ViewModel获取
        List<HealthCard> cards = new ArrayList<>();
        cards.add(new HealthCard(R.string.diet_record, "今天摄入了1800千卡", android.R.drawable.ic_menu_edit));
        cards.add(new HealthCard(R.string.exercise_record, "今天运动消耗了300千卡", android.R.drawable.ic_menu_compass));
        cards.add(new HealthCard(R.string.water_record, "今天已喝水1500ml", android.R.drawable.ic_menu_info_details));
        cards.add(new HealthCard(R.string.sleep_record, "昨晚睡眠7小时30分钟", android.R.drawable.ic_menu_recent_history));
        cards.add(new HealthCard(R.string.weight_record, "体重：65kg", android.R.drawable.ic_menu_report_image));
        cards.add(new HealthCard(R.string.step_record, "今日步数：8500步", android.R.drawable.ic_menu_directions));
        return cards;
    }
} 