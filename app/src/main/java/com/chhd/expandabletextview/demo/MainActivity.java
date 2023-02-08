package com.chhd.expandabletextview.demo;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.DynamicLayout;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.chhd.expandabletextview.ExpandableTextView;
import com.chhd.superlayout.SuperTextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

//        String text = "花旗：英国秋季报告或减轻抵押贷款利率上升压力\n花旗分析师在一份报告中表示英国政府在周四的秋季报告中宣布的支出削减和增税计划“旨在降低抵押贷款利率”。分析人士称，英国财政大臣亨特的计划(包括增税和削减支出约550亿英镑)减轻了利率上升的压力，这意味着抵押贷款利率上升的压力将会更小。";
//        String text = "花旗：英国秋季报告或减轻抵押贷款利率\n上升压力\n花旗分析师在一份报告中表示英国政府在周四的秋季报告中宣布的支出削减和增税计划“旨在降低抵押贷款利率”。分析人士称，英国财政大臣亨特的计划(包括增税和削减支出约550亿英镑)减轻了利率上升的压力，这意味着抵押贷款利率上升的压力将会更小。";

//        String text = "<b>安徽亳州：多子女家庭购新房首次申请公积金贷款额度可上浮10万元<br/>金十数据1月10日讯，亳州市发布《关于支持多子女家庭使用住房公积金贷款的通知》。按照新政，对符合国家生育政策生育的多子女家庭，在亳州市购买新建商品住房且首次申请住房公积金贷款的，贷款最高额度可按家庭当期最高贷款额度限额上浮10万元确定，上浮后的贷款额度不得超过亳州市规定的公积金贷款额度上限。(金十数据APP)</b>";
        String text = "<b>安徽亳州：多子女家庭购新房首次申请公积金贷款额度可上浮10万元金十数据1月10日讯，亳州市发布《关于支持多子女家庭使用住房公积金贷款的通知》。按照新政，对符合国家生育政策生育的多子女家庭，在亳州市购买新建商品住房且首次申请住房公积金贷款的，贷款最高额度可按家庭当期最高贷款额度限额上浮10万元确定，上浮后的贷款额度不得超过亳州市规定的公积金贷款额度上限。(金十数据APP)</b>";
//        text = delTags(text);
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(text);
        ssb.setSpan(new ForegroundColorSpan(Color.GREEN), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        CharSequence html = Html.fromHtml(text);
        Log.i(TAG, "onCreate html: " + html + ", text: " + text);
        etv.setText(html/*, ExpandableTextView.STATE_SHRINK*/);
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
//        rv.setAdapter(adapter);

        final TextView tvPrimitive = findViewById(R.id.tv_primitive);
        tvPrimitive.setText(text);
        tvPrimitive.post(new Runnable() {
            @Override
            public void run() {
                DynamicLayout mLayout = new DynamicLayout(tvPrimitive.getText(), tvPrimitive.getPaint(), tvPrimitive.getWidth(),
                        Layout.Alignment.ALIGN_NORMAL,
                        tvPrimitive.getLineSpacingMultiplier(), tvPrimitive.getLineSpacingExtra(), tvPrimitive.getIncludeFontPadding());

                Log.i(TAG, "onCreate 2: " + tvPrimitive.getLineCount() + ", " + mLayout.getLineCount()
                        + ", " + tvPrimitive.getHeight() + ", " + mLayout.getHeight());
            }
        });
    }

    public static String delTags(String content) {
        if (content == null) {
            return "";
        }
        content = content.replaceAll("<br[\\s]*/?>", "\n").replaceAll("&nbsp;", " ");
//        return content.replaceAll("<[^>]+>", "").trim();
        String copyContent = content;
        Pattern p = Pattern.compile("<[^>]+>");
        Matcher matcher = p.matcher(content);
        while (matcher.find()) {
            String label = matcher.group();
            if ("<b>".equals(label) || "</b>".equals(label)) {

            } else {
                copyContent = copyContent.replace(label, "");
            }
        }
        return replaceWrongUnicode(copyContent.trim(), "");
    }

    public static String replaceWrongUnicode(String source, String replace) {
        if (TextUtils.isEmpty(source)) {
            return source;
        }
        if (TextUtils.isEmpty(replace)) {
            replace = "";
        }
        Pattern CRLF = Pattern.compile("([\\u007f-\\u009f]|\\u00ad|[\\u0483-\\u0489]|[\\u0559-\\u055a]|\\u058a|[\\u0591-\\u05bd]|\\u05bf|[\\u05c1-\\u05c2]|[\\u05c4-\\u05c7]|[\\u0606-\\u060a]|[\\u063b-\\u063f]|\\u0674|[\\u06e5-\\u06e6]|\\u070f|[\\u076e-\\u077f]|\\u0a51|\\u0a75|\\u0b44|[\\u0b62-\\u0b63]|[\\u0c62-\\u0c63]|[\\u0ce2-\\u0ce3]|[\\u0d62-\\u0d63]|\\u135f|[\\u200b-\\u200f]|[\\u2028-\\u202e]|\\u2044|\\u2071|[\\uf701-\\uf70e]|[\\uf710-\\uf71a]|\\ufb1e|[\\ufc5e-\\ufc62]|\\ufeff|\\ufffc)");
        Matcher m = CRLF.matcher(source);
        if (m.find()) {
            return m.replaceAll(replace);
        }
        return source;
    }

    public void onBtn1Click(View v) {
//        etv.setMaxLinesOnShrink(new Random().nextInt(7) + 1);
//        adapter.setMaxLinesOnShrink(new Random().nextInt(7) + 1);
//        etv.setEllipsisHintColor(Color.GREEN);
        etvStatus.clear();
        canFold = !canFold;
        rv.setAdapter(new Adapter());
//        rv.setAdapter(adapter);
//        adapter.notifyDataSetChanged();
    }

    public void onBtn2Click(View v) {
//        etv.setExpandHintColor(Color.RED);
//        rv.setAdapter(new Adapter());
//        SuperTextView textView = new SuperTextView(this);
//        textView.setText("精英");
//        textView.setBackground(getResources().getDrawable(R.drawable.bg_fillet_red));
//        textView.setTextColor(Color.RED);
//        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
//        textView.setGravity(Gravity.CENTER);
//        textView.getSuperHelper().setRoundCorner(SizeUtils.dp2px(2));
//        textView.getSuperHelper().setStrokeColor(Color.RED);
//        textView.setPadding(SizeUtils.dp2px(4), SizeUtils.dp2px(2), SizeUtils.dp2px(4), SizeUtils.dp2px(2));
//        textView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
//                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//        textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());
//        textView.setDrawingCacheEnabled(true);
//        textView.buildDrawingCache();
//        Bitmap bitmap = textView.getDrawingCache();
//        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
//        Log.i(TAG, "onBtn2Click: " + textView.getMeasuredWidth() + ", " + bitmap.getWidth() + ", " + drawable.getIntrinsicWidth());
//        etv.setToExpandIcon(drawable);
//        etv.setToExpandIcon(getResources().getDrawable(R.mipmap.ic_expand));
//        etv.setToShrinkIcon(getResources().getDrawable(R.mipmap.ic_shrink));
    }

    public void onBtn3Click(View v) {
        etv.setShrinkHintColor(Color.BLUE);
    }

    int titleWidth;
    int etvWidth;
    boolean canFold = true;

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
            if (canFold) {
                status = status == null ? ExpandableTextView.STATE_SHRINK : status;
            } else {
                status = status == null ? ExpandableTextView.STATE_EXPAND : status;
            }
            etv.setToShrinkHint(canFold ? "收起" : "");
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
