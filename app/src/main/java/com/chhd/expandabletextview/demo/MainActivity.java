package com.chhd.expandabletextview.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chhd.expandabletextview.ExpandableTextView;

public class MainActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private SparseArray<Integer> etvStatus = new SparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.top).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), MainActivity.class);
                startActivity(intent);
            }
        });

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                ExpandableTextView tv = findViewById(R.id.tv);
//                tv.setText(getString(R.string.text2));
//            }
//        }, 1000);

        RecyclerView rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new Adapter());
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.Holder> {

        private int etvWidth;

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new Holder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_text, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final Holder holder, int i) {
            final ExpandableTextView etv = holder.itemView.findViewById(R.id.etv);
//            etv.post(new Runnable() {
//                @Override
//                public void run() {
//                    etvWidth = etv.getWidth();
//                }
//            });
//            etv.updateForRecyclerView(getString(R.string.text), etvWidth, status);
            Integer status = etvStatus.get(holder.getLayoutPosition());
            status = status == null ? ExpandableTextView.STATE_SHRINK : status;
            etv.setText(getString(R.string.text3), status);
            etv.setExpandListener(new ExpandableTextView.OnExpandListener() {
                @Override
                public void onExpand(ExpandableTextView view) {
                    etvStatus.put(holder.getLayoutPosition(), view.getExpandState());
                }

                @Override
                public void onShrink(ExpandableTextView view) {
                    etvStatus.put(holder.getLayoutPosition(), view.getExpandState());
                }
            });
//            etv.setJZText(getString(R.string.text));
        }

        @Override
        public int getItemCount() {
            return 20;
        }

        public class Holder extends RecyclerView.ViewHolder {
            public Holder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}
