package com.chhd.expandabletextview.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.DynamicLayout;
import android.text.Layout;
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
    private ExpandableTextView etv;
    private RecyclerView rv;
    private Adapter adapter = new Adapter();

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

        etv = findViewById(R.id.etv);

        String text = "花旗：英国秋季报告或减轻抵押贷款利率上升压力\n花旗分析师在一份报告中表示英国政府在周四的秋季报告中宣布的支出削减和增税计划“旨在降低抵押贷款利率”。分析人士称，英国财政大臣亨特的计划(包括增税和削减支出约550亿英镑)减轻了利率上升的压力，这意味着抵押贷款利率上升的压力将会更小。";
//        String text = "花旗：英国秋季报告或减轻抵押贷款利率\n上升压力\n花旗分析师在一份报告中表示英国政府在周四的秋季报告中宣布的支出削减和增税计划“旨在降低抵押贷款利率”。分析人士称，英国财政大臣亨特的计划(包括增税和削减支出约550亿英镑)减轻了利率上升的压力，这意味着抵押贷款利率上升的压力将会更小。";
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(text);
        ssb.setSpan(new ForegroundColorSpan(Color.GREEN), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        etv.setText(text, ExpandableTextView.STATE_SHRINK);
        etv.setOnChildClickListener(new ExpandableTextView.OnChildClickListener() {
            @Override
            public void onContentClick(ExpandableTextView view, int state) {
                Log.i(TAG, "onContentClick: " + view);
            }

            @Override
            public void onExpandClick(ExpandableTextView view, int state) {
                Log.i(TAG, "onExpandClick: ");
                view.expand();
            }

            @Override
            public void onShrinkClick(ExpandableTextView view, int state) {
                Log.i(TAG, "onShrinkClick: ");
                view.shrink();
            }
        });
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

        rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        final TextView tvPrimitive = findViewById(R.id.tv_primitive);
        tvPrimitive.setText(text);
        tvPrimitive.post(new Runnable() {
            @Override
            public void run() {
                DynamicLayout mLayout = new DynamicLayout(tvPrimitive.getText(), tvPrimitive.getPaint(), tvPrimitive.getWidth(),
                        Layout.Alignment.ALIGN_NORMAL,
                        tvPrimitive.getLineSpacingMultiplier(), tvPrimitive.getLineSpacingExtra(), tvPrimitive.getIncludeFontPadding());

                Log.i(TAG, "onCreate 2: " + tvPrimitive.getLineCount() + ", " + mLayout.getLineCount() + ", " + tvPrimitive.getHeight() + ", " + mLayout.getHeight());
            }
        });
    }

    public void onBtn1Click(View v) {
//        etv.setMaxLinesOnShrink(new Random().nextInt(7) + 1);
//        adapter.setMaxLinesOnShrink(new Random().nextInt(7) + 1);
//        etv.setEllipsisHintColor(Color.GREEN);
        rv.setAdapter(new Adapter());
//        rv.setAdapter(adapter);
//        adapter.notifyDataSetChanged();
    }

    public void onBtn2Click(View v) {
        etv.setExpandHintColor(Color.RED);
    }

    public void onBtn3Click(View v) {
        etv.setShrinkHintColor(Color.BLUE);
    }

    private int titleWidth;
    private int etvWidth;

    public class Adapter extends RecyclerView.Adapter<Adapter.Holder> {

        int maxLine = 0;

        public void setMaxLinesOnShrink(int maxLine) {
            this.maxLine = maxLine;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new Holder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_text, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final Holder holder, int i) {
            final TextView tvTitle = holder.itemView.findViewById(R.id.tv_title);
            final MyETV etv = holder.itemView.findViewById(R.id.etv);
            tvTitle.post(new Runnable() {
                @Override
                public void run() {
                    titleWidth = tvTitle.getWidth();
                }
            });
            etv.post(new Runnable() {
                @Override
                public void run() {
                    etvWidth = etv.getWidth();
                }
            });
            Integer status = etvStatus.get(holder.getLayoutPosition());
            status = status == null ? ExpandableTextView.STATE_SHRINK : status;
            etv.setTvHeader(tvTitle, titleWidth);
            etv.setText(holder.getAdapterPosition() + ", " + getString(R.string.text), status, etvWidth);
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
            if (maxLine > 0) {
                etv.setMaxLinesOnShrink(maxLine);
            }
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
