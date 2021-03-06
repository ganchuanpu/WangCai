package com.wangcai.lottery.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.wangcai.lottery.R;
import com.wangcai.lottery.app.BaseFragment;
import com.wangcai.lottery.app.FragmentLauncher;
import com.wangcai.lottery.app.WangCaiApp;
import com.wangcai.lottery.base.net.GsonHelper;
import com.wangcai.lottery.base.net.RestCallback;
import com.wangcai.lottery.base.net.RestRequest;
import com.wangcai.lottery.base.net.RestRequestManager;
import com.wangcai.lottery.base.net.RestResponse;
import com.wangcai.lottery.component.CustomDialog;
import com.wangcai.lottery.component.DialogLayout;
import com.wangcai.lottery.data.BetData;
import com.wangcai.lottery.data.ChaseRowData;
import com.wangcai.lottery.data.Lottery;
import com.wangcai.lottery.data.OrderFind;
import com.wangcai.lottery.data.PayMoneyCommand;
import com.wangcai.lottery.data.ProfitMarginRowData;
import com.wangcai.lottery.data.RedoubleRowData;
import com.wangcai.lottery.data.SameMultipleRowData;
import com.wangcai.lottery.data.Trace;
import com.wangcai.lottery.data.TraceIssue;
import com.wangcai.lottery.data.TraceIssueCommand;
import com.wangcai.lottery.data.UserInfo;
import com.wangcai.lottery.data.UserInfoCommand;
import com.wangcai.lottery.material.ChaseRuleData;
import com.wangcai.lottery.material.ConstantInformation;
import com.wangcai.lottery.material.ShoppingCart;
import com.wangcai.lottery.pattern.TaskPlanView;
import com.wangcai.lottery.pattern.TitleTimingSalesView;
import com.wangcai.lottery.user.UserCentre;
import com.wangcai.lottery.view.adapter.ProfitMarginAdapter;
import com.wangcai.lottery.view.adapter.RedoubleAdapter;
import com.wangcai.lottery.view.adapter.SameMultipleAdapter;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;

/**
 * 追号
 * Created by ACE-PC on 2018/7/7.
 */
public class ChaseFragment extends BaseFragment implements RadioRealButtonGroup.OnPositionChangedListener, TaskPlanView.OnArrangeChangedListener {
    private static final String TAG = ChaseFragment.class.getSimpleName();
    private static final int BUY_TRACE_ID = 1;
    private static final int ID_TRACE_CHASE_ISSUE_INFO = 3;

    /**
     * 　信息提示
     */
    private static final int TRACK_TURNED_PAGE_LOGIN = 1;
    private static final int TRACK_TURNED_PAGE_RECHARGE = 2;
    private static final int TRACK_TURNED_PAGE_PICK = 3;
    private static final int TRACK_PROMPT_TIP = 4;
    private static final int ID_USER_INFO = 5;

    @BindView(R.id.mode_radiogroup)
    RadioRealButtonGroup modeRadiogroup;
    @BindView(R.id.chase_timing)
    View chaseTiming;
    @BindView(R.id.plan_ruleview)
    CardView planLayout;
    @BindView(R.id.chaseTitle)
    CardView chaseTitle; //标题
    @BindView(R.id.chaselistview)
    ListView chaseListView; //页面列表
    @BindView(R.id.chase_mete)
    TextView chaseMete;
    @BindView(R.id.chase_amount)
    TextView chaseAmount;
    @BindView(R.id.chase_balance)
    TextView chaseBalance;
    @BindView(R.id.plan_buybutton)
    Button buybutton;

    private Lottery lottery;        //当前彩种
    private UserCentre userCentre;  //用户数据中心
    private ShoppingCart cart;      //购物车
    private SameMultipleAdapter sameMultipleAdapter;    //ListView 列表视图
    private ProfitMarginAdapter profitMarginAdapter;
    private RedoubleAdapter redoubleAdapter;
    private TaskPlanView planView;  //任务设置视图
    private TitleTimingSalesView timingView;
    private ArrayList<TraceIssue> traceIssues = new ArrayList<>();      //全部奖期数据
    private List<TraceIssue> issueList = new ArrayList<>();             //任务奖期列表
    private SparseArray<Integer> multipleArray = new SparseArray<>();   //任务倍数
    private SparseArray<Boolean> chooseArray = new SparseArray<>();
    private List<String> serialsList = new ArrayList<>();
    private int selectPage = -1;
    private Map<Integer, ChaseRowData<SameMultipleRowData>> sameMultipleRows = new HashMap();
    private Map<Integer, ChaseRowData<ProfitMarginRowData>> profitMarginRows = new HashMap();
    private Map<Integer, ChaseRowData<RedoubleRowData>> redoubleRows = new HashMap();

    //cost 当前成本　grandTotal 累计投入　Bonus 资金 total 累计利润　TotalProfit 利润率 Total Cost 购物车总价  addUpAmount 累计金额
    private double bonus = 0, totalCost = 0, addUpAmount = 0, cost = 0, tempAmount = 0;
    //multiple 倍数　tagType 模式(0自定义模式、1利润率模式)
    private int tagType = 0, share = 0;
    //同倍追号、利润率追号、翻倍追号
    private TraceIssue traceIssue;
    private String currentIssue = "";       //当前奖期

    public static void launch(BaseFragment fragment, Lottery lottery, String issue) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("lottery", lottery);
        bundle.putString("issue", issue);
        FragmentLauncher.launch(fragment.getActivity(), ChaseFragment.class, bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflateView(inflater, container, "智能追号", R.layout.fragment_chase, true, true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parameter();
        loadTimingView();
        initInfo();
    }

    private void parameter() {
        this.lottery = (Lottery) getArguments().getSerializable("lottery");
        this.cart = ShoppingCart.getInstance();
        this.userCentre = WangCaiApp.getUserCentre();
    }

    private void loadTimingView() {
        timingView = new TitleTimingSalesView(getActivity(), findViewById(R.id.chase_timing), lottery);
        timingView.setOnSalesIssueListener((String salesIssue, boolean status) -> {
            ShoppingCart.getInstance().setIssue(salesIssue);
            if (status) { //初始化 false
                UpdateUI(false);
            }
        });
    }

    private void initInfo() {
        cart.init(lottery);
        setTitle(lottery.getName());
        planPrompt(0, 0);
        modeRadiogroup.setOnPositionChangedListener(this);
    }

    @OnClick(R.id.plan_buybutton)
    public void CheckOut() {
        if (timingView.getIssue() == null || timingView.getIssue().length() <= 0) {
            tipDialog("温馨提示", "请稍等，正在加载销售奖期信息……", 0);
            return;
        }

        // ①判断：购物车中是否有投注
        if (!cart.isEmpty()) {
            // ②判断：用户是否登录——被动登录
            if (userCentre.isLogin()) {
                // ③判断：用户的余额是否满足投注需求
                if (cart.getPlanAmount() <= userCentre.getUserInfo().getAbalance()) {
                    // ④界面跳转：跳转到追期和倍投的设置界面
                    /*DanTiaoUtils danTiaoUtils=new DanTiaoUtils();
                    String danTiaoString=danTiaoUtils.isShowDialogChase(lottery.getLotteryId());*/
//                    if(TextUtils.isEmpty(danTiaoString)){
                    verificationData();
//                    }else{
//                        tipDialog2("提示",danTiaoString);
//                        return;
//                    }
                } else {
                    // 提示用户：充值去；界面跳转：用户充值界面
                    tipDialog("温馨提示", "请充值", TRACK_TURNED_PAGE_RECHARGE);
                }
            } else {
                // 提示用户：登录去；界面跳转：用户登录界面
                tipDialog("温馨提示", "请重新登录", TRACK_TURNED_PAGE_LOGIN);
            }
        } else {
            // 提示用户需要选择一注
            tipDialog("温馨提示", "您需要选择一注", TRACK_TURNED_PAGE_PICK);
        }
    }

    /**
     *
     */
    private void verificationData() {
        if (traceIssues.size() == 0 || timingView.getIssue().isEmpty()) {
            return;
        }
        int index = traceIssues.indexOf(timingView.getIssue());
        if (!additionalVerified()) {
            if (traceIssues.size() < planView.getIssueNo()) {
                int nois = traceIssues.size() - index;
                planView.setIssueNo(String.valueOf(nois));
                tipDialog("温馨提示", "最大追期" + nois, 0);
            } else {
                tipDialog("温馨提示", "请生成追号", 0);
            }
        }

        if ((issueList.size() == 0 || multipleArray.size() == 0) && issueList.size() != multipleArray.size()) {
            tipDialog("温馨提示", "数据不匹配", 0);
            return;
        }

        /*String chooseCodes = "";
        for (Ticket tk : cart.getCodesMap()) {
            chooseCodes += tk.getChooseMethod().getCname() + "\t" + tk.getCodes() + "\t";
        }*/

        String msg = getActivity().getResources().getString(R.string.is_shopping_list_chasetip);
        msg = StringUtils.replaceEach(msg, new String[]{"STATUS", "ISSUE", "NOTE", "UNIT", "DOUBLE", "CHASENUM", "MONEY"},
                new String[]{planView.isStopOnWin() ? "是" : "否", timingView.getIssue(), String.valueOf(cart.getPlanNotes()),
                        cart.getLucreMode().getName(), "" + planView.getMultiple(), "" + cart.getTraceNumber(), String.format("%.3f", tempAmount)});

        BetData betData = new BetData();
        betData.setGameId(lottery.getId());
        betData.setIsTrace(1);
        betData.setTraceWinStop(planView.isStopOnWin());
        betData.setBalls(cart.getCodeData());
        betData.setOrders(cart.getOrdersMap());
        betData.setAmount(cart.getPlanAmount());

        try {
            String bet = GsonHelper.toJson(betData).toString();
            PayMoneyCommand payMoneyCommand = new PayMoneyCommand();
            payMoneyCommand.setBetData(bet);
            payMoneyCommand.setSign(DigestUtils.md5Hex(URLEncoder.encode("action=bet&betdata=" + bet + "&packet=Game&token=" + userCentre.getUserInfo().getToken(), "UTF-8") + userCentre.getKeyApiKey()));

            if (!timingView.getIssue().isEmpty()) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.popup_buy_succeed, null);
                ImageView icon = view.findViewById(R.id.icon);
                icon.setImageResource(ConstantInformation.getLotteryLogo(lottery.getIdentifier(), true));
                TextView name = view.findViewById(R.id.name);
                name.setText(lottery.getName());
                TextView issue = view.findViewById(R.id.issue);
                issue.setText("起始期号:" + timingView.getIssue());
                TextView info = view.findViewById(R.id.info);
                info.setText(Html.fromHtml(msg).toString());

                CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
                builder.setContentView(view);
                builder.setTitle("温馨提示");
                builder.setLayoutSet(DialogLayout.SINGLE);
                builder.setPositiveButton("确认投注", (dialog, which) -> {
                    buybutton.setEnabled(false);
                    PayCommand(payMoneyCommand);
                    dialog.dismiss();
                });
                builder.create().show();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private boolean additionalVerified() {
        boolean additionalStatus = true;
        switch (tagType) {
            case 0:
                if (planView.getIssueNo() == 0 || planView.getIssueNo() != sameMultipleRows.size()) {
                    additionalStatus = false;
                }
                break;
            case 1:
                if (planView.getIssueNo() == 0 || planView.getIssueNo() != profitMarginRows.size()) {
                    additionalStatus = false;
                }
                break;
            case 2:
                if (planView.getIssueNo() == 0 || planView.getIssueNo() != redoubleRows.size()) {
                    additionalStatus = false;
                }
                break;
        }
        return additionalStatus;
    }

    private void PayCommand(PayMoneyCommand paycommand) {
        TypeToken typeToken = new TypeToken<RestResponse<OrderFind>>() {
        };
        RestRequest restRequest = RestRequestManager.createRequest(getActivity(), paycommand, typeToken, restCallback, BUY_TRACE_ID, this);
        restRequest.execute();
    }

    private void receiptOrderDialog(final OrderFind orderFind) {
        cart.clear();
        cleanData();
        cleanViews();
        CustomDialog.Builder builder = new CustomDialog.Builder(getContext());
        builder.setMessage(orderFind.getId() > 0 ? "投注成功" : "投注失败!请重新投注");
        builder.setTitle(orderFind.getId() > 0 ? "投注成功" : "投注失败");
        builder.setLayoutSet(DialogLayout.UP_AND_DOWN);
        builder.setNegativeBackground(R.drawable.notidialog_bottom_leftandright_fillet_btn_s);
        builder.setNegativeButton("查看投注记录", (dialog, which) -> {
            dialog.dismiss();
            if (orderFind.getId() > 0) {
                Trace trace = new Trace();
                trace.setId(orderFind.getId());
                trace.setLotteryId(lottery.getId());
                BetOrTraceDetailFragment.launch(ChaseFragment.this, trace);
            }
            getActivity().finish();
        });
        builder.setPositiveBackground(R.drawable.notidialog_bottom_rectangle_btn_s);
        builder.setPositiveButton("继续投注", (dialog, which) -> {
            getActivity().finish();
            dialog.dismiss();
        });
        CustomDialog dialog = builder.create();
        dialog.setOnDismissListener((d) -> getActivity().finish());
        dialog.show();
    }

    /**
     * 底部信息显示区
     *
     * @param cmete
     * @param ctotal
     */
    private void planPrompt(int cmete, double ctotal) {
        String isCmeteTips = getContext().getResources().getString(R.string.is_chasemete_tips);
        isCmeteTips = StringUtils.replaceEach(isCmeteTips, new String[]{"CHASEMETE"}, new String[]{String.format("%d", cmete)});
        chaseMete.setText(Html.fromHtml(isCmeteTips));

        String isAmountTips = getContext().getResources().getString(R.string.is_chaseamount_tips);
        isAmountTips = StringUtils.replaceEach(isAmountTips, new String[]{"CHASETOTAL"}, new String[]{String.format("%.3f", ctotal)});
        chaseAmount.setText(Html.fromHtml(isAmountTips));

        String isCtotalTips = getContext().getResources().getString(R.string.is_balance_tips);
        isCtotalTips = StringUtils.replaceEach(isCtotalTips, new String[]{"BALANCE"}, new String[]{String.format("%.3f", userCentre.getUserInfo().getAbalance())});
        chaseBalance.setText(Html.fromHtml(isCtotalTips));
    }

    /**
     * 选择某选项卡
     */
    private void selectPage(int position) {
        if (traceIssues.size() == 0) {
            return;
        }
        if (position == 1 && cart.getCountMethod().size() > 1) {
            tipDialog("温馨提示", "多个玩法不支持利润追号", 0);
            modeRadiogroup.getChildAt(1).setEnabled(false);
        } else {
            this.tagType = position;
            planPrompt(0, 0);
            planRuleView(tagType);
            createRowLayout();
        }
    }

    /**
     * 初始化添加 追号设置与标题UI
     */
    private void planRuleView(int position) {
        planLayout.removeAllViews();
        chaseTitle.removeAllViews();
        View ruleView, titleView;
        AppCompatCheckBox checkBox;
        switch (position) {
            case 0: //同倍追号
                ruleView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_chase_samemultiple_rule, null);
                titleView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_chase_samemultiple_title, null);
                sameMultipleAdapter = new SameMultipleAdapter();
                checkBox = titleView.findViewById(R.id.check_box);
                checkBox.setChecked(true);
                checkBox.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                    Map<Integer, ChaseRowData<SameMultipleRowData>> sameMultipleMap = sameMultipleRows;
                    for (Map.Entry<Integer, ChaseRowData<SameMultipleRowData>> entry : sameMultipleMap.entrySet()) {
                        ChaseRowData<SameMultipleRowData> chaseRow = entry.getValue();
                        chaseRow.setCheckBoxStatus(isChecked);
                        chaseRow.set(chaseRow.get());
                        sameMultipleRows.put(entry.getKey(), chaseRow);
                    }
                    sameMultipleAdapter.UpdateUIData(sameMultipleRows, multipleArray);
                });
                break;
            case 1: //利润追号
                ruleView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_chase_profit_rule, null);
                titleView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_chase_profit_title, null);
                profitMarginAdapter = new ProfitMarginAdapter();
                checkBox = titleView.findViewById(R.id.check_box);
                checkBox.setChecked(true);
                checkBox.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                    Map<Integer, ChaseRowData<ProfitMarginRowData>> profitMarginMap = profitMarginRows;
                    for (Map.Entry<Integer, ChaseRowData<ProfitMarginRowData>> entry : profitMarginMap.entrySet()) {
                        ChaseRowData<ProfitMarginRowData> chaseRow = entry.getValue();
                        chaseRow.setCheckBoxStatus(isChecked);
                        chaseRow.set(chaseRow.get());
                        profitMarginRows.put(entry.getKey(), chaseRow);
                    }
                    profitMarginAdapter.UpdateUIData(profitMarginRows, multipleArray);
                });
                break;
            case 2: //翻倍追号
                ruleView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_chase_redouble_rule, null);
                titleView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_chase_redouble_title, null);
                redoubleAdapter = new RedoubleAdapter();
                checkBox = titleView.findViewById(R.id.check_box);
                checkBox.setChecked(true);
                checkBox.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                    Map<Integer, ChaseRowData<RedoubleRowData>> redoubleRowMap = redoubleRows;
                    for (Map.Entry<Integer, ChaseRowData<RedoubleRowData>> entry : redoubleRowMap.entrySet()) {
                        ChaseRowData<RedoubleRowData> chaseRow = entry.getValue();
                        chaseRow.setCheckBoxStatus(isChecked);
                        chaseRow.set(chaseRow.get());
                        redoubleRows.put(entry.getKey(), chaseRow);
                    }
                    redoubleAdapter.UpdateUIData(redoubleRows, issueList, multipleArray);
                });
                break;
            default: //同倍追号
                ruleView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_chase_samemultiple_rule, null);
                titleView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_chase_samemultiple_title, null);
                sameMultipleAdapter = new SameMultipleAdapter();
                checkBox = titleView.findViewById(R.id.check_box);
                checkBox.setChecked(true);
                checkBox.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                    Map<Integer, ChaseRowData<SameMultipleRowData>> sameMultipleMap = sameMultipleRows;
                    for (Map.Entry<Integer, ChaseRowData<SameMultipleRowData>> entry : sameMultipleMap.entrySet()) {
                        ChaseRowData<SameMultipleRowData> chaseRow = entry.getValue();
                        chaseRow.setCheckBoxStatus(isChecked);
                        chaseRow.set(chaseRow.get());
                        sameMultipleRows.put(entry.getKey(), chaseRow);
                    }
                    sameMultipleAdapter.UpdateUIData(sameMultipleRows, multipleArray);
                });
        }
        planView = new TaskPlanView(getActivity(), ruleView, position);
        planView.setOnArrangeChangedListener(this);
        planLayout.addView(ruleView);
        chaseTitle.addView(titleView);
    }

    /**
     * 生成数据
     */
    private void createRowLayout() {
        if (cart.getPlanNotes() == 0 || traceIssues.size() == 0 || timingView.getIssue().isEmpty()) {
            return;
        }

        if (planView.getMultiple() == 0 || planView.getIssueNo() == 0) {
            tipDialog("温馨提示", "倍数和追号期数不能为0", 0);
            return;
        }

        if (cart.getPrizeMode() == 0) {
            tipDialog("温馨提示", "请选择奖金模式", 0);
            return;
        }
        UpdateUI(true);
    }

    /**
     * 更新UI页面
     */
    private void UpdateUI(boolean dupe) {
        if (cart.getMethodArray().size() == 1) {
            this.bonus = cart.getMethodArray().get(0).getMaxPrize() * cart.getLucreMode().getFactor();
            this.serialsList = getIssueSerials();
            UpdateData(dupe);
        }
    }

    /**
     * 更新视图数据
     *
     * @param dupe
     */
    private void UpdateData(boolean dupe) {
        ChaseRuleData rule = this.cart.getChaseRule();
        if (dupe) {
            for (int i = 0, size = serialsList.size(); i < size; i++) {
                multipleArray.put(i, rule.getMultiple());
                chooseArray.put(i, true);
            }
        }
        this.totalCost = 0;
        this.cart.setPlanBuyRule();
        switch (tagType) {
            case 0:
                totalCost = sameMultiple(rule);
                break;
            case 1:
                totalCost = getGrowthType1(rule);
                /*switch (rule.getType()) {
                    case 0:
                        totalCost = getGrowthType(rule);
                        break;
                    case 1:
                        totalCost = getBalancedRatioType(rule);
                        break;
                    case 2:
                        totalCost = getBonusType(rule);
                        break;
                    case 3:
                        totalCost = getBalancedIncomeType(rule);
                        break;
                }*/
                break;
            case 2:
                totalCost = getRedoubleType(rule);
                break;
        }

        if (serialsList.size() == 0) {
            planPrompt(0, 0);
        } else {
            switch (tagType) {
                case 0:
                    sameMultipleAdapter.UpdateUIData(sameMultipleRows, multipleArray);
                    sameMultipleAdapter.setOnItemListener((ChaseRowData rowData, int pos, boolean status) -> {
                        chooseArray.put(pos, status);
                        sameMultipleRows.put(pos, rowData);
                        statistics();
                    });
                    sameMultipleAdapter.setOnItemMultipleListener((SparseArray<Integer> multiple) -> {
                        this.multipleArray = multiple;
                        UpdateData(false);
                    });
                    chaseListView.setAdapter(sameMultipleAdapter);
                    break;
                case 1:
                    profitMarginAdapter.UpdateUIData(profitMarginRows, multipleArray);
                    profitMarginAdapter.setOnItemListener((ChaseRowData rowData, int pos, boolean status) -> {
                        chooseArray.put(pos, status);
                        profitMarginRows.put(pos, rowData);
                        statistics();
                    });
                    profitMarginAdapter.setOnItemMultipleListener((SparseArray<Integer> multiple) -> {
                        this.multipleArray = multiple;
                        UpdateData(false);
                    });
                    chaseListView.setAdapter(profitMarginAdapter);
                    break;
                case 2:
                    redoubleAdapter.UpdateUIData(redoubleRows, issueList, multipleArray);
                    redoubleAdapter.setOnItemListener((ChaseRowData rowData, int pos, boolean status) -> {
                        chooseArray.put(pos, status);
                        redoubleRows.put(pos, rowData);
                        statistics();
                    });
                    redoubleAdapter.setOnItemMultipleListener((SparseArray<Integer> multiple) -> {
                        this.multipleArray = multiple;
                        UpdateData(false);
                    });
                    chaseListView.setAdapter(redoubleAdapter);
                    break;
            }
            /*统计最终订单**/
            statistics();
            /*20171005 gan 修复追 *  期 超过一定数的时候 界面回调 start*/
            planView.setIssueNo(String.valueOf(planView.getIssueNo()));
            /*20171005 gan 修复追 *  期 超过一定数的时候 界面回调 end*/
        }
    }

    /**
     * 统计最终订单，更新数据
     */
    private void statistics() {
        Map<String, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < serialsList.size(); i++) {
            boolean chooseStatus = chooseArray.get(i);
            if (chooseStatus) {
                orderMap.put(serialsList.get(i), multipleArray.get(i));
            }
        }

        this.cart.setTraceOrdersMap(orderMap.size(), orderMap);
        double total = 0;
        for (int i = 0, size = serialsList.size(); i < size; i++) {
            if (!chooseArray.get(i)) {
                total += (multipleArray.get(i) * cart.getPlanAmount() * cart.getLucreMode().getFactor());
            }
        }
        this.tempAmount = totalCost - total;
        planPrompt(cart.getTraceNumber(), tempAmount);
    }

    @Override
    public void onPositionChanged(RadioRealButton button, int currentPosition, int lastPosition) {
        switch (currentPosition) {
            case 0:
                selectPage = 0;
                selectPage(0);  //同倍追号
                break;
            case 1:
                selectPage = 1;
                selectPage(1);  //利润追号
                break;
            case 2:
                selectPage = 2;
                selectPage(2);  //翻倍追号
                break;
        }
    }

    @Override
    public void newArrange(ChaseRuleData chaserule, boolean status, boolean dupe) {
        hideSoftKeyBoard();
        arrange(chaserule, dupe);
    }

    //任务设置数据返回
    private void arrange(ChaseRuleData chaserule, boolean dupe) {
        cart.setChaseRule(chaserule);
        if (sameMultipleRows.size() == 0) {
            return;
        }
        share = 0;
        if (dupe && !isFinishing()) {
            UpdateUI(dupe);
        }
    }

    /**
     * 同倍追号 (自定义)
     *
     * @param rule
     * @return
     */
    private double sameMultiple(ChaseRuleData rule) {
        this.sameMultipleRows = new HashMap<>();
        this.cost = cart.getPlanAmount() * rule.getMultiple();
        this.addUpAmount = cost;
        boolean fool = true;
        for (int i = 0, size = serialsList.size(); i < size; i++) {
            ChaseRowData chaseRow = new ChaseRowData<SameMultipleRowData>();
            int mlp = multipleArray.get(i);
            double mustBonus = bonus * mlp;

            if (traceIssue.getLimitAmount() > 0 && traceIssue.getLimitAmount() < mustBonus) {
                share = i;
                int multiplier = 0; //倍数计算
                if (i != 0) {
                    multiplier = (int) (traceIssue.getLimitAmount() / bonus - addUpAmount / cart.getPlanAmount());
                } else {
                    multiplier = (int) (traceIssue.getLimitAmount() / bonus);
                }
                multipleArray.put(i, multiplier);
                cost = cart.getPlanAmount() * multiplier;
                addUpAmount = addUpAmount + cost;
                if (fool) {
                    fool = false;
                    tipDialog("温馨提示", "该计划超出限额，将自动为您调整目标", TRACK_PROMPT_TIP);
                }
            } else {
                cost = cart.getPlanAmount() * mlp;
                if (i != 0) {
                    this.addUpAmount += cost;
                } else {
                    this.addUpAmount = cost;
                }
            }
            chaseRow.setCheckBoxStatus(chooseArray.get(i));
            chaseRow.set(new SameMultipleRowData(i, serialsList.get(i), separateString(serialsList.get(i)), multipleArray.get(i), cost, addUpAmount));
            sameMultipleRows.put(i, chaseRow);
        }
        return addUpAmount;
    }

    /**
     * 全程利润率
     *
     * @param rule
     * @return
     */
    private double getGrowthType1(ChaseRuleData rule) {
        this.profitMarginRows = new HashMap<>();
        this.cost = cart.getPlanAmount() * rule.getMultiple();
        this.addUpAmount = cost;
        boolean fool = true;
        StringBuilder stopString = new StringBuilder();
        //cost 当前成本、　grandTotal 累计投入、　Bonus 资金、 total 累计利润、　totalProfit 利润率、 totalCost 购物车总价
        for (int i = 0, size = serialsList.size(); i < size; i++) {
            ChaseRowData chaseRow = new ChaseRowData<ProfitMarginRowData>();
            if (share == 0) {
                int mlp = multipleArray.get(i);
                double mustBonus = bonus * mlp;
                cost = cart.getPlanAmount() * mlp;
                if (i != 0) {
                    this.addUpAmount += cost;
                } else {
                    this.addUpAmount = cost;
                }
                double totalProfit = (mustBonus - addUpAmount) / addUpAmount * 100; //计算净利率
                if (totalProfit < planView.getPlanWay()) {
                    double predict = 0;
                    int plus = 0;
                    if (i != 0) {
                        plus = multipleArray.get(i - 1);
                    } else {
                        plus = multipleArray.get(i);
                    }
                    while (true) {
                        if (traceIssue.getLimitAmount() > 0 && traceIssue.getLimitAmount() < mustBonus) {
                            share = i;
                            if (fool) {
                                fool = false;
                                tipDialog("温馨提示", "该计划超出无法实现，请调整目标", TRACK_PROMPT_TIP);
                            }
                            break;
                        }
                        predict = addUpAmount + (cart.getPlanAmount() * plus - cost);
                        mustBonus = bonus * plus;
                        totalProfit = (mustBonus - predict) / predict * 100;
                        if (totalProfit >= planView.getPlanWay()) {
                            addUpAmount = predict; //净利润
                            cost = cart.getPlanAmount() * plus; //改变当前投入金额
                            multipleArray.put(i, plus);
                            break;
                        } else {
                            plus = plus + 1;
                        }
                    }
                }
                double profitLoss = mustBonus - addUpAmount;               //奖金 * 倍数－累计投入＝中奖盈亏
                chaseRow.setCheckBoxStatus(chooseArray.get(i));
                chaseRow.set(new ProfitMarginRowData(i, serialsList.get(i), separateString(serialsList.get(i)), multipleArray.get(i), cost, addUpAmount, mustBonus, profitLoss, totalProfit));
                profitMarginRows.put(i, chaseRow);
            } else {
                stopString.append(i).append(",");
            }
        }
        if (stopString.length() > 0) {
            maxFilter(stopString.substring(0, stopString.length() - 1).split(","));
        }
        return addUpAmount;
    }

    /**
     * 全程利润率
     *
     * @param rule
     * @return
     */
    private double getGrowthType(ChaseRuleData rule) {
        this.profitMarginRows = new HashMap<>();
        this.cost = cart.getPlanAmount() * rule.getMultiple();
        this.addUpAmount = cost;
        boolean fool = true;
        StringBuilder stopString = new StringBuilder();
        //cost 当前成本、　grandTotal 累计投入、　Bonus 资金、 total 累计利润、　totalProfit 利润率、 totalCost 购物车总价
        for (int i = 0, size = serialsList.size(); i < size; i++) {
            ChaseRowData chaseRow = new ChaseRowData<ProfitMarginRowData>();
            if (share == 0) {
                int mlp = multipleArray.get(i);
                double mustBonus = bonus * mlp;
                cost = cart.getPlanAmount() * mlp;
                if (i != 0) {
                    this.addUpAmount += cost;
                } else {
                    this.addUpAmount = cost;
                }
                double totalProfit = (mustBonus - addUpAmount) / addUpAmount * 100; //计算净利率
                if (totalProfit < rule.getGainMode()) {
                    double predict = 0;
                    int plus = 0;
                    if (i != 0) {
                        plus = multipleArray.get(i - 1);
                    } else {
                        plus = multipleArray.get(i);
                    }
                    while (true) {
                        if (traceIssue.getLimitAmount() > 0 && traceIssue.getLimitAmount() < mustBonus) {
                            share = i;
                            if (fool) {
                                fool = false;
                                tipDialog("温馨提示", "该计划超出无法实现，请调整目标", TRACK_PROMPT_TIP);
                            }
                            break;
                        }
                        plus = plus + 1;
                        predict = addUpAmount + (cart.getPlanAmount() * plus - cost);
                        mustBonus = bonus * plus;
                        totalProfit = (mustBonus - predict) / predict * 100;
                        if (totalProfit >= rule.getGainMode()) {
                            addUpAmount = predict; //净利润
                            cost = cart.getPlanAmount() * plus; //改变当前投入金额
                            multipleArray.put(i, plus);
                            break;
                        }
                    }
                }
                double profitLoss = bonus - addUpAmount;               //奖金 * 倍数－累计投入＝中奖盈亏
                chaseRow.setCheckBoxStatus(chooseArray.get(i));
                chaseRow.set(new ProfitMarginRowData(i, serialsList.get(i), separateString(serialsList.get(i)), multipleArray.get(i), cost, addUpAmount, mustBonus, profitLoss, keepThree(totalProfit).intValue()));
                profitMarginRows.put(i, chaseRow);
            } else {
                stopString.append(i).append(",");
            }
        }
        if (stopString.length() > 0) {
            maxFilter(stopString.substring(0, stopString.length() - 1).split(","));
        }
        return addUpAmount;
    }

    /**
     * 均衡型 (利润率)
     *
     * @param rule
     * @return
     */
    private double getBalancedRatioType(ChaseRuleData rule) {
        this.profitMarginRows = new HashMap<>();
        this.cost = cart.getPlanAmount() * rule.getMultiple();
        this.addUpAmount = cost;
        boolean fool = true;
        StringBuilder stopString = new StringBuilder();
        for (int i = 0, size = serialsList.size(); i < size; i++) {
            ChaseRowData chaseRow = new ChaseRowData<ProfitMarginRowData>();
            if (share == 0) {
                int mlp = multipleArray.get(i);
                double mustBonus = bonus * mlp;
                cost = cart.getPlanAmount() * mlp;

                if (i != 0) {
                    this.addUpAmount += cost;
                } else {
                    this.addUpAmount = cost;
                }
                double totalProfit = (mustBonus - addUpAmount) / addUpAmount * 100; //计算净利率
                if (i < rule.getIssueGap()) {
                    if (totalProfit < rule.getAgoValue()) {
                        double predict = 0;
                        int plus = 0;
                        if (i != 0) {
                            plus = multipleArray.get(i - 1);
                        } else {
                            plus = multipleArray.get(i);
                        }
                        while (true) {
                            if (traceIssue.getLimitAmount() > 0 && traceIssue.getLimitAmount() < mustBonus) {
                                share = i;
                                if (fool) {
                                    fool = false;
                                    tipDialog("温馨提示", "该计划超出无法实现，请调整目标", TRACK_PROMPT_TIP);
                                }
                                break;
                            }
                            plus = plus + 1;
                            predict = addUpAmount + (cart.getPlanAmount() * plus - cost);
                            mustBonus = bonus * plus;
                            totalProfit = (mustBonus - predict) / predict * 100;
                            if (totalProfit >= rule.getAgoValue()) {
                                addUpAmount = predict; //净利润
                                cost = cart.getPlanAmount() * plus; //改变当前投入金额
                                multipleArray.put(i, plus);
                                break;
                            }
                        }
                    }
                } else if (i >= rule.getIssueGap()) {
                    if (totalProfit < rule.getLaterValue()) {
                        double predict = 0;
                        int plus = 0;
                        if (i != 0) {
                            plus = multipleArray.get(i - 1);
                        } else {
                            plus = multipleArray.get(i);
                        }
                        while (true) {
                            if (traceIssue.getLimitAmount() > 0 && traceIssue.getLimitAmount() < mustBonus) {
                                share = i;
                                if (fool) {
                                    fool = false;
                                    tipDialog("温馨提示", "该计划超出无法实现，请调整目标", TRACK_PROMPT_TIP);
                                }
                                break;
                            }
                            plus = plus + 1;
                            predict = addUpAmount + (cart.getPlanAmount() * plus - cost);
                            mustBonus = bonus * plus;
                            totalProfit = (mustBonus - predict) / predict * 100;
                            if (totalProfit >= rule.getLaterValue()) {
                                addUpAmount = predict; //净利润
                                cost = cart.getPlanAmount() * plus; //改变当前投入金额
                                multipleArray.put(i, plus);
                                break;
                            }
                        }
                    }
                }
                double profitLoss = bonus - addUpAmount; //奖金 * 倍数－累计投入＝中奖盈亏
                chaseRow.setCheckBoxStatus(chooseArray.get(i));
                chaseRow.set(new ProfitMarginRowData(i, serialsList.get(i), separateString(serialsList.get(i)), multipleArray.get(i), cost, addUpAmount, mustBonus, profitLoss, keepThree(totalProfit).intValue()));
                profitMarginRows.put(i, chaseRow);
            } else {
                stopString.append(i).append(",");
            }
        }
        if (stopString.length() > 0) {
            maxFilter(stopString.substring(0, stopString.length() - 1).split(","));
        }
        return addUpAmount;
    }

    /**
     * 全程利润(元)
     *
     * @param rule
     * @return
     */
    private double getBonusType(ChaseRuleData rule) {
        this.profitMarginRows = new HashMap<>();
        this.cost = cart.getPlanAmount() * rule.getMultiple();
        this.addUpAmount = cost;
        boolean fool = true;
        StringBuilder stopString = new StringBuilder();
        for (int i = 0, size = serialsList.size(); i < size; i++) {
            ChaseRowData chaseRow = new ChaseRowData<ProfitMarginRowData>();
            if (share == 0) {
                int mlp = multipleArray.get(i);
                double mustBonus = bonus * mlp;
                cost = cart.getPlanAmount() * mlp;
                if (i != 0) {
                    this.addUpAmount += cost;
                } else {
                    this.addUpAmount = cost;
                }
                double total = mustBonus - addUpAmount;
                if (total < rule.getGainMode()) {
                    int plus = 0;
                    if (i != 0) {
                        plus = multipleArray.get(i - 1);
                    } else {
                        plus = multipleArray.get(i);
                    }
                    while (true) {
                        if (traceIssue.getLimitAmount() > 0 && traceIssue.getLimitAmount() < mustBonus) {
                            share = i;
                            if (fool) {
                                fool = false;
                                tipDialog("温馨提示", "该计划超出无法实现，请调整目标", TRACK_PROMPT_TIP);
                            }
                            break;
                        }
                        plus = plus + 1;
                        mustBonus = bonus * plus;
                        total = mustBonus - (i != 0 ? addUpAmount + cart.getPlanAmount() * plus -
                                cost : cart.getPlanAmount() * plus);
                        if (total > rule.getGainMode()) {
                            if (i != 0) {
                                addUpAmount = addUpAmount + cart.getPlanAmount() * plus - cost;
                                //6+2*3=12
                            } else {
                                addUpAmount = cart.getPlanAmount() * plus; //2*3=6
                            }
                            cost = cart.getPlanAmount() * plus;
                            multipleArray.put(i, plus);
                            break;
                        }
                    }
                }
                double totalProfit = (total / addUpAmount) * 100;
                double profitLoss = bonus - addUpAmount; //奖金 * 倍数－累计投入＝中奖盈亏
                chaseRow.setCheckBoxStatus(chooseArray.get(i));
                chaseRow.set(new ProfitMarginRowData(i, serialsList.get(i), separateString(serialsList.get(i)), multipleArray.get(i), cost, addUpAmount, mustBonus, profitLoss, keepThree(totalProfit).intValue()));
                profitMarginRows.put(i, chaseRow);
            } else {
                stopString.append(i).append(",");
            }
        }
        if (stopString.length() > 0) {
            maxFilter(stopString.substring(0, stopString.length() - 1).split(","));
        }
        return addUpAmount;
    }

    /**
     * 均衡型 (利润)
     *
     * @param rule
     * @return
     */
    private double getBalancedIncomeType(ChaseRuleData rule) {
        this.profitMarginRows = new HashMap<>();
        this.cost = cart.getPlanAmount() * rule.getMultiple();
        this.addUpAmount = cost;
        boolean fool = true;
        StringBuilder stopString = new StringBuilder();
        for (int i = 0, size = serialsList.size(); i < size; i++) {
            ChaseRowData chaseRow = new ChaseRowData<ProfitMarginRowData>();
            if (share == 0) {
                int mlp = multipleArray.get(i);
                double mustBonus = bonus * mlp;
                cost = cart.getPlanAmount() * mlp;

                if (i != 0) {
                    this.addUpAmount += cost;
                } else {
                    this.addUpAmount = cost;
                }
                double total = mustBonus - addUpAmount;
                if (i < rule.getIssueGap()) {
                    if (total < rule.getAgoValue()) {
                        int plus = 0;
                        if (i != 0) {
                            plus = multipleArray.get(i - 1);
                        } else {
                            plus = multipleArray.get(i);
                        }
                        while (true) {
                            if (traceIssue.getLimitAmount() > 0 && traceIssue.getLimitAmount() < mustBonus) {
                                share = i;
                                if (fool) {
                                    fool = false;
                                    tipDialog("温馨提示", "该计划超出无法实现，请调整目标", TRACK_PROMPT_TIP);
                                }
                                break;
                            }
                            plus = plus + 1;
                            mustBonus = bonus * plus;
                            total = mustBonus - (i != 0 ? addUpAmount + cart.getPlanAmount() * plus
                                    - cost : cart.getPlanAmount() * plus);
                            if (total > rule.getAgoValue()) {
                                if (i != 0) {
                                    addUpAmount = addUpAmount + cart.getPlanAmount() * plus - cost;
                                    //6+2*3=12
                                } else {
                                    addUpAmount = cart.getPlanAmount() * plus; //2*3=6
                                }
                                cost = cart.getPlanAmount() * plus;
                                multipleArray.put(i, plus);
                                break;
                            }
                        }
                    }
                } else if (i >= rule.getIssueGap()) {
                    if (total < rule.getLaterValue()) {
                        int plus = 0;
                        if (i != 0) {
                            plus = multipleArray.get(i - 1);
                        } else {
                            plus = multipleArray.get(i);
                        }
                        while (true) {
                            if (traceIssue.getLimitAmount() > 0 && traceIssue.getLimitAmount() < mustBonus) {
                                share = i;
                                if (fool) {
                                    fool = false;
                                    tipDialog("温馨提示", "该计划超出无法实现，请调整目标", TRACK_PROMPT_TIP);
                                }
                                break;
                            }
                            plus = plus + 1;
                            mustBonus = bonus * plus;
                            total = mustBonus - (i != 0 ? addUpAmount + cart.getPlanAmount() * plus
                                    - cost : cart.getPlanAmount() * plus);
                            if (total > rule.getLaterValue()) {
                                if (i != 0) {
                                    addUpAmount = addUpAmount + cart.getPlanAmount() * plus - cost;
                                    //6+2*3=12
                                } else {
                                    addUpAmount = cart.getPlanAmount() * plus; //2*3=6
                                }
                                cost = cart.getPlanAmount() * plus;
                                multipleArray.put(i, plus);
                                break;
                            }
                        }
                    }
                }
                double totalProfit = (total / addUpAmount) * 100;
                double profitLoss = bonus - addUpAmount; //奖金 * 倍数－累计投入＝中奖盈亏
                chaseRow.setCheckBoxStatus(chooseArray.get(i));
                chaseRow.set(new ProfitMarginRowData(i, serialsList.get(i), separateString(serialsList.get(i)), multipleArray.get(i), cost, addUpAmount, mustBonus, profitLoss, keepThree(totalProfit).intValue()));
                profitMarginRows.put(i, chaseRow);
            } else {
                stopString.append(i).append(",");
            }
        }
        if (stopString.length() > 0) {
            maxFilter(stopString.substring(0, stopString.length() - 1).split(","));
        }
        return addUpAmount;
    }

    /**
     * 加倍追号 每隔几期
     *
     * @param rule
     * @return
     */
    private double getRedoubleType(ChaseRuleData rule) {
        multipleArray.clear();
        int setup = 0, hierarchy = planView.getMultiple();
        for (int i = 0, size = serialsList.size(); i < size; i++) {
            if (setup < planView.getDivideIssue()) {//小于 隔期
                setup = setup + 1;
            } else {  //大于 隔期
                setup = 0;
                hierarchy = hierarchy * planView.getDivideMultiple();
                setup = setup + 1;
            }
            multipleArray.put(i, hierarchy);
        }
        this.redoubleRows = new HashMap<>();
        this.cost = cart.getPlanAmount() * rule.getMultiple();
        this.addUpAmount = cost;
        boolean fool = true;
        StringBuilder stopString = new StringBuilder();
        for (int i = 0, size = serialsList.size(); i < size; i++) {
            ChaseRowData chaseRow = new ChaseRowData<RedoubleRowData>();
            if (share == 0) {
                int mlp = multipleArray.get(i);
                double mustBonus = bonus * mlp;
                if (traceIssue.getLimitAmount() > 0 && traceIssue.getLimitAmount() < mustBonus) {
                    share = i;
                    multipleArray.put(i, mlp);
                    cost = cart.getPlanAmount() * mlp;
                    addUpAmount = addUpAmount + cost;
                    if (fool) {
                        fool = false;
                        tipDialog("温馨提示", "该计划超出无法实现，请调整目标", TRACK_PROMPT_TIP);
                    }
                } else {
                    cost = cart.getPlanAmount() * mlp;
                    if (i != 0) {
                        this.addUpAmount += cost;
                    } else {
                        this.addUpAmount = cost;
                    }
                }
                chaseRow.setCheckBoxStatus(chooseArray.get(i));
                chaseRow.set(new RedoubleRowData(i, serialsList.get(i), separateString(serialsList.get(i)), multipleArray.get(i), cost, ""));
                redoubleRows.put(i, chaseRow);
            } else {
                stopString.append(i).append(",");
            }
        }
        if (stopString.length() > 0) {
            maxFilter(stopString.substring(0, stopString.length() - 1).split(","));
        }
        return addUpAmount;
    }

    //清除倍元素
    private void removeRow(int position) {
        switch (tagType) {
            case 0:
                sameMultipleRows.remove(position);
                break;
            case 1:
                profitMarginRows.remove(position);
                break;
            case 2:
                redoubleRows.remove(position);
                break;
        }
    }

    /**
     * 过滤最大追号
     *
     * @param removeRow
     */
    private void maxFilter(String[] removeRow) {
        if (share == 0 && (removeRow == null || removeRow.length == 0)) {
            return;
        }

        for (int i = 0, size = removeRow.length; i < size; i++) {
            switch (tagType) {
                case 0:
                    sameMultipleRows.remove(removeRow[i]);
                    break;
                case 1:
                    profitMarginRows.remove(removeRow[i]);
                    break;
                case 2:
                    redoubleRows.remove(removeRow[i]);
                    break;
            }
        }
    }

    /**
     * 处理小数点的位数
     *
     * @param d
     * @return
     */
    public BigDecimal keepThree(double d) {
        if (Double.isInfinite(d)) {
            BigDecimal bg = new BigDecimal(0.00);
            return bg.setScale(3, BigDecimal.ROUND_HALF_UP);
        } else {
            BigDecimal bg = new BigDecimal(d);
            return bg.setScale(3, BigDecimal.ROUND_HALF_UP);
        }
    }

    /**
     * 解析追号期数
     *
     * @return
     */
    private List<String> getIssueSerials() {
        if (!TextUtils.isEmpty(timingView.getIssue())) {
            if (traceIssues.size() < planView.getIssueNo()) {
                int index = -1;
                for (int i = 0; i < traceIssues.size(); i++) {
                    TraceIssue t = traceIssues.get(i);
                    if (t.getNumber().contains(timingView.getIssue())) {
                        traceIssue = t;
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    issueList = traceIssues.subList(index, traceIssues.size() - index);
                    ConstantInformation.setIssueSerials(issueList);
                }
            } else {
                int index = -1;
                for (int i = 0; i < traceIssues.size(); i++) {
                    TraceIssue t = traceIssues.get(i);
                    if (t.getNumber().contains(timingView.getIssue())) {
                        traceIssue = t;
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    issueList = traceIssues.subList(index, index + planView.getIssueNo());
                    ConstantInformation.setIssueSerials(issueList);
                }
            }
        }
        return ConstantInformation.getIssueSerials();
    }

    /**
     * 分析期数字符串
     *
     * @param issue
     * @return
     */
    private String separateString(String issue) {
        int splitNo = 0;
        switch (lottery.getId()) {
            case 13:
            case 14:
            case 17:
            case 19:
            case 20:
                splitNo = 4;
                break;
            case 2:
            case 6:
            case 8:
            case 9:
            case 22:
            case 27:
            case 32:
                splitNo = 2;
                break;
            default: //  1 7 10 11 12 15 16 18 21 28 30 33 39 41 42 44 48 49 57 59
                splitNo = 3;
        }
        int index = issue.indexOf("-");
        String issuestr = "";
        if (index != -1) {
            issuestr = issue.substring(index + 1, issue.length());
        } else {
            if (issue.length() > splitNo) {
                issuestr = issue.substring(issue.length() - splitNo, issue.length());
            } else {
                issuestr = issue;
            }
        }
        return issuestr;
    }

    /**
     * 信息提示
     *
     * @param title
     * @param msg
     * @param track
     */
    private void tipDialog(String title, String msg, final int track) {
        CustomDialog.Builder builder = new CustomDialog.Builder(getContext());
        builder.setMessage(msg);
        builder.setTitle(title);
        builder.setLayoutSet(DialogLayout.SINGLE);
        builder.setPositiveButton("知道了", (dialog, which) -> {
            dialog.dismiss();
            if (track == TRACK_PROMPT_TIP) {
                planPrompt(0, 0);
            }
            if (track == TRACK_TURNED_PAGE_PICK) {
                getActivity().finish();
            }
        });
        builder.create().show();
    }

    /**
     * 追号期数请求
     */
    private void chaseIssueCommand() {
        TraceIssueCommand command = new TraceIssueCommand();
        command.setLotteryId(lottery.getId());
        TypeToken typeToken = new TypeToken<RestResponse<ArrayList<TraceIssue>>>() {
        };
        RestRequest restRequest = RestRequestManager.createRequest(getActivity(), command, typeToken, restCallback, ID_TRACE_CHASE_ISSUE_INFO, this);
        restRequest.execute();
    }

    private RestCallback restCallback = new RestCallback() {
        @Override
        public boolean onRestComplete(RestRequest request, RestResponse response) {
            switch (request.getId()) {
                case BUY_TRACE_ID:
                    OrderFind orderFind = (OrderFind) response.getData();
                    executeCommand(new UserInfoCommand(), restCallback, ID_USER_INFO);
                    if (orderFind != null) {
                        buybutton.setEnabled(true);
                        receiptOrderDialog(orderFind);
                    }
                    break;
                case ID_USER_INFO:
                    UserInfo userInfo = ((UserInfo) response.getData());
                    userCentre.setUserInfo(userInfo);
                    planPrompt(0, 0);
                    break;
                case ID_TRACE_CHASE_ISSUE_INFO:
                    traceIssues = (ArrayList<TraceIssue>) response.getData();
                    selectPage(selectPage != -1 ? selectPage : 0);
                    break;
            }
            return true;
        }

        @Override
        public boolean onRestError(RestRequest request, int errCode, String errDesc) {
            buybutton.setEnabled(true);
            return false;
        }

        @Override
        public void onRestStateChanged(RestRequest request, @RestRequest.RestState int state) {

        }
    };

    private void cleanViews() {
        //chaseLVLayout.removeAllViews();
        switch (tagType) {
            case 0:
                sameMultipleAdapter.UpdateUIData(new HashMap<>(), new SparseArray<>());
                chaseListView.setAdapter(sameMultipleAdapter);
                break;
            case 1:
                profitMarginAdapter.UpdateUIData(new HashMap<>(), new SparseArray<>());
                chaseListView.setAdapter(profitMarginAdapter);
                break;
            case 2:
                redoubleAdapter.UpdateUIData(new HashMap<>(), new ArrayList<>(), new SparseArray<>());
                chaseListView.setAdapter(redoubleAdapter);
                break;
        }
    }

    private void cleanData() {
        switch (tagType) {
            case 0:
                sameMultipleRows.clear();
                break;
            case 1:
                profitMarginRows.clear();
                break;
            case 2:
                redoubleRows.clear();
                break;
        }
        multipleArray.clear();
        serialsList.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        chaseIssueCommand();
    }

    @Override
    public void onDestroyView() {
        cart.setChaseRule(new ChaseRuleData());
        if (timingView != null) {
            timingView.stop();
        }
        super.onDestroyView();
    }

}
