package com.chhd.expandabletextview.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
//                Intent intent = new Intent(getApplication(), MainActivity.class);
//                startActivity(intent);
            }
        });

        final ExpandableTextView tv = findViewById(R.id.tv);

        String text = "花旗：英国秋季报告或减轻抵押贷款利率上升压力\n花旗分析师在一份报告中表示英国政府在周四的秋季报告中宣布的支出削减和增税计划“旨在降低抵押贷款利率”。分析人士称，英国财政大臣亨特的计划(包括增税和削减支出约550亿英镑)减轻了利率上升的压力，这意味着抵押贷款利率上升的压力将会更小。";
//        SpannableStringBuilder ssb = new SpannableStringBuilder();
//        ssb.append(text);
//        ssb.setSpan(new ForegroundColorSpan(Color.GREEN), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(text, ExpandableTextView.STATE_SHRINK);
//        tv.setOnChildClickListener(new ExpandableTextView.OnChildClickListener() {
//            @Override
//            public void onContentClick(ExpandableTextView view, int state) {
//                Log.i(TAG, "onContentClick: " + view);
//            }
//
//            @Override
//            public void onExpandClick(ExpandableTextView view, int state) {
//                Log.i(TAG, "onExpandClick: ");
//            }
//
//            @Override
//            public void onShrinkClick(ExpandableTextView view, int state) {
//                Log.i(TAG, "onShrinkClick: ");
//            }
//        });
//        tv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i(TAG, "onClick: " + v);
//            }
//        });
//        tv.setMovementMethod(LinkMovementMethod.getInstance());
//        tv.setHighlightColor(Color.TRANSPARENT);

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
            etv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.i(TAG, "onLongClick: ");
                    return true;
                }
            });
            etv.setOnChildClickListener(new ExpandableTextView.OnChildClickListener() {
                @Override
                public void onContentClick(ExpandableTextView view, int state) {
                    Log.i(TAG, "onContentClick: " + state);
                }

                @Override
                public void onExpandClick(ExpandableTextView view, int state) {
                    Log.i(TAG, "onExpandClick: " + state + ", " + etv.getExpandState());
                    etv.expand();
                    Log.i(TAG, "onExpandClick: " + state + ", " + etv.getExpandState());
                }

                @Override
                public void onShrinkClick(ExpandableTextView view, int state) {
                    Log.i(TAG, "onShrinkClick: " + state + ", " + etv.getExpandState());
                    etv.shrink();
                    Log.i(TAG, "onShrinkClick: " + state + ", " + etv.getExpandState());
                }
            });
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
