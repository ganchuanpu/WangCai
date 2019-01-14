package com.wangcai.lottery.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.michael.easydialog.EasyDialog;
import com.umeng.analytics.MobclickAgent;
import com.wangcai.lottery.R;
import com.wangcai.lottery.app.BaseFragment;
import com.wangcai.lottery.app.FragmentLauncher;
import com.wangcai.lottery.app.WangCaiApp;
import com.wangcai.lottery.base.net.GsonHelper;
import com.wangcai.lottery.base.net.JsonString;
import com.wangcai.lottery.base.net.RestCallback;
import com.wangcai.lottery.base.net.RestRequest;
import com.wangcai.lottery.base.net.RestRequestManager;
import com.wangcai.lottery.base.net.RestResponse;
import com.wangcai.lottery.component.CustomDialog;
import com.wangcai.lottery.component.DialogLayout;
import com.wangcai.lottery.component.DiscreteSeekBar;
import com.wangcai.lottery.component.FlowRadioGroup;
import com.wangcai.lottery.data.Bet;
import com.wangcai.lottery.data.BetData;
import com.wangcai.lottery.data.GetSinglePickCommand;
import com.wangcai.lottery.data.Lottery;
import com.wangcai.lottery.data.OrderFind;
import com.wangcai.lottery.data.PayMoneyCommand;
import com.wangcai.lottery.data.RateGroup;
import com.wangcai.lottery.data.RegChildRebate;
import com.wangcai.lottery.data.RegChildRebateCommand;
import com.wangcai.lottery.data.Trace;
import com.wangcai.lottery.data.TraceIssue;
import com.wangcai.lottery.data.TraceIssueCommand;
import com.wangcai.lottery.data.UserInfo;
import com.wangcai.lottery.data.UserInfoCommand;
import com.wangcai.lottery.material.ConstantInformation;
import com.wangcai.lottery.material.ShoppingCart;
import com.wangcai.lottery.pattern.ChooseTips;
import com.wangcai.lottery.pattern.ShroudView;
import com.wangcai.lottery.pattern.TitleTimingSalesView;
import com.wangcai.lottery.user.UserCentre;
import com.wangcai.lottery.util.DanTiaoUtils;
import com.google.gson.reflect.TypeToken;
import com.michael.easydialog.EasyDialog;
import com.umeng.analytics.MobclickAgent;
import com.wangcai.lottery.util.SignUtils;
import com.wangcai.lottery.view.adapter.ShoppingAdapter;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.OnClick;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.MaterialHeader;

/**
 * 购物车
 * Created on 2016/02/17.
 * @author ACE
 */
public class ShoppingFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = ShoppingFragment.class.getSimpleName();

    private static final int BUY_TRACE_ID = 1;
    private static final int ID_USER_INFO = 2;
    private static final int BUY_REBATE_ID = 3;
    private static final int BUY_TRACE_ISSUE_ID = 4;
    private static final int GET_SINGLE_PICK = 5;
    private static final int TRACK_TURNED_PAGE_LOGIN = 1;
    private static final int TRACK_TURNED_PAGE_RECHARGE = 2;
    private static final int TRACK_TURNED_PAGE_PICK = 3;
    private static final int TRACK_TURNED_PAGE_DANTIAO= 4;//单挑


    @BindView(R.id.customize_toolbar)
    RelativeLayout customizeToolbar;
    @BindView(R.id.add_menu)
    ImageView addMenu;
    @BindView(R.id.ptrFrameLayout)
    PtrFrameLayout ptrFrameLayout;
    @BindView(R.id.shopping_list)
    ListView shoppingList;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.lottery_shopping_balance)
    TextView balanceText;
    @BindView(R.id.lottery_shopping_buy)
    Button shopping_buy;
    @BindView(R.id.chaseadd_number_layout)
    View chaseaddNumberLayout;
    @BindView(R.id.select_mode)
    ImageView select_mode;
    @BindView(R.id.setting_mode)
    TextView setting_mode;

    private Handler handler = new Handler();
    private TitleTimingSalesView timingView;

    private MaterialHeader materialHeader;
    private ShoppingAdapter adapter;
    private ShroudView shroudView;
    private ChooseTips chooseTips;
    private Lottery lottery;
    private ShoppingCart cart;
    private UserCentre userCentre;
    private boolean isInTraceState;
    private RegChildRebate rebate;
    /**
     * 辅助用，投注异常时，上报到服务器的错误信息
     */
    private String unusualInfo;

    public static void launch(BaseFragment fragment, Lottery lottery) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("lottery", lottery);
        FragmentLauncher.launch(fragment.getActivity(), ShoppingFragment.class, bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return inflateView(inflater, container, "投注", R.layout.shopping_fragment, true, true);
        return inflater.inflate(R.layout.shopping_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parameter();
        initInfo();
        loadFunction();
        loadLotteryRebate();
        planPrompt();
		setSettingModeTextview(WangCaiApp.getUserCentre().getLucreMode());
        GetSinglePick();
    }



    private void GetSinglePick() {
        GetSinglePickCommand command = new GetSinglePickCommand();
        executeCommand(command, restCallback, GET_SINGLE_PICK);
    }

	 private void setSettingModeTextview(int position) {

        switch (position) {
            case 0:
                setting_mode.setText("2元");
                break;
            case 1:
                setting_mode.setText("1元");
                break;
            case 2:
                setting_mode.setText("2角");
                break;
            case 3:
                setting_mode.setText("1角");
                break;
            case 4:
                setting_mode.setText("2分");
                break;
            case 5:
                setting_mode.setText("2厘");
                break;
        }

    }

    private void parameter() {
        lottery = (Lottery) getArguments().getSerializable("lottery");
        userCentre = WangCaiApp.getUserCentre();
        cart = ShoppingCart.getInstance();
    }

    private void initInfo() {
        cart.init(lottery);
        setTitle(lottery.getName());
        balanceText.setText(String.format("余额：%.4f", userCentre.getUserInfo().getAbalance()));
        if (lottery.getId() == 12 || lottery.getId() == 11) {
            //福彩3D 和 P3p5 不允许追号
            chaseaddNumberLayout.setVisibility(View.GONE);
        }
    }

    private void initHeaders() {
        materialHeader = new MaterialHeader(getContext());
        materialHeader.setColorSchemeColors(new int[]{Color.RED, Color.GREEN, Color.BLUE});//类似SwipeRefreshLayout
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            executeCommand(new UserInfoCommand(), restCallback, ID_USER_INFO);
        }
    }

    private void loadFunction() {
//        addMenu.setEnabled(false);
        shroudView = new ShroudView(findViewById(R.id.shopping_bottom));
        chooseTips = new ChooseTips(findViewById(R.id.shopping_choosetip));
        timingView = new TitleTimingSalesView(getActivity(), findViewById(R.id.shopping_top_timing), lottery);
        timingView.setOnSalesIssueListener((String issue,boolean status) -> {
            ShoppingCart.getInstance().setIssue(issue);
            loadTraceIssue();
        });
        adapter = new ShoppingAdapter();
        adapter.setOnDeleteClickListener(this::planPrompt);
        shoppingList.setAdapter(adapter);

        initHeaders();
        ptrFrameLayout.setHeaderView(materialHeader);//类似SwipeRefreshLayout
        ptrFrameLayout.addPtrUIHandler(materialHeader);

        ptrFrameLayout.setPtrHandler(new PtrHandler() {

            //检查是否可以刷新
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return true;
            }

            //开始刷新
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                progressBar.setVisibility(View.VISIBLE);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //第3.2步：
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                        ptrFrameLayout.refreshComplete();
                    }
                }, 400);
            }
        });

        shroudView.setModeChosenListener((int multiple, int chaseadd) -> {
            cart.setPlanBuyRule(multiple, chaseadd);
            planPrompt();
            hideSoftKeyBoard();
        });
        shroudView.setOnChaseFragmentListener(() -> {
            if (cart.getCountMethod().size() == 1 && !TextUtils.isEmpty(timingView.getIssue())) {
                ChaseFragment.launch(ShoppingFragment.this, lottery, timingView.getIssue());
            } else {
                //TODO:提示追号
                if (cart.getCountMethod().size() > 1 || cart.getCountMethod().size() == 0) {
                    tipDialog("温馨提示", cart.getCountMethod().size() > 1 ? "追号只支持单种玩法投注" : "您需要选择一注", TRACK_TURNED_PAGE_PICK);
                    return;
                }
            }
        });
    }

    @OnClick(R.id.chase_button)
    public void chase() {
        if (timingView.getIssue() == null || timingView.getIssue().length() <= 0) {
            tipDialog("温馨提示", "请稍等，正在加载销售奖期信息……", 0);
            return;
        }
        if (cart.getPlanNotes() == 0) {
            tipDialog("温馨提示", "您需要选择一注", TRACK_TURNED_PAGE_PICK);
            return;
        }
    }

    private void showBubble() {
        View bubbleView = getLayoutInflater().inflate(R.layout.setup_bet_bubble, null);
        FlowRadioGroup viewGroup = bubbleView.findViewById(R.id.lucremode_sett);
        DiscreteSeekBar discreteSeekBar1 = bubbleView.findViewById(R.id.discrete1);
        TextView rebateset = bubbleView.findViewById(R.id.rebate_text);
        TextView bonusset = bubbleView.findViewById(R.id.bonus_text);
        viewGroup.setItemChecked(WangCaiApp.getUserCentre().getLucreMode());
        viewGroup.setOnCheckChangedListener((FlowRadioGroup group, int position, boolean checked) -> {
            viewGroup.setItemChecked(position);
            WangCaiApp.getUserCentre().setLucreMode(position);
            setSettingModeTextview(position);
            updatePlan();
        });
        int min = 0, max = 0;
        if (rebate != null && rebate.getRate() != null && rebate.getRate().size() > 0) {
            min = rebate.getRate().get(0).getPrizeGroup();
            max = rebate.getRate().get(rebate.getRate().size() - 1).getPrizeGroup();
        }

        discreteSeekBar1.setMin(min);
        discreteSeekBar1.setMax(max);
        if(WangCaiApp.getUserCentre().getPrizeGroup()>0){
            discreteSeekBar1.setProgress(WangCaiApp.getUserCentre().getPrizeGroup());
        }else {
            discreteSeekBar1.setProgress(max);
        }

        discreteSeekBar1.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                if (rebate != null && rebate.getRate() != null && rebate.getRate().size() > 0) {
                    for (Iterator<RateGroup> it = rebate.getRate().iterator(); it.hasNext(); ) {
                        RateGroup rateGroup = it.next();
                        if (value == rateGroup.getPrizeGroup()) {
                            BigDecimal b = new BigDecimal(rateGroup.getRate());
                            rebateset.setText(b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue() + "%");
                            bonusset.setText(String.format("%d", rateGroup.getPrizeGroup()));
                        }
                    }
                } else {
                    rebateset.setText(String.format("%.2f%%", 0.00));
                    bonusset.setText(String.format("%d", 0));
                }
                WangCaiApp.getUserCentre().setPrizeGroup(value);
                updatePlan();
                return value;
            }
        });

        new EasyDialog(getActivity())
                .setLayout(bubbleView)
                .setGravity(EasyDialog.GRAVITY_TOP)
                .setBackgroundColor(getResources().getColor(R.color.background))
                .setLocationByAttachedView(select_mode)
                .setAnimationTranslationShow(EasyDialog.DIRECTION_X, 350, 400, 100, -50, 50, 0)
                .setAnimationAlphaShow(350, 0.3f, 1.0f)
                .setAnimationTranslationDismiss(EasyDialog.DIRECTION_X, 350, -50, 400)
                .setAnimationAlphaDismiss(350, 1.0f, 0.0f)
                .setTouchOutsideDismiss(true)
                .setMatchParent(false)
                .setMarginLeftAndRight(24, 24)
                .setOutsideColor(getResources().getColor(R.color.halfTransparent))
                .show();
    }


    private void updatePlan() {
        cart.setPlanBuyRule();
        planPrompt();
    }

    public void planPrompt() {
        chooseTips.setTipsText(String.format("\t%.3f\t 元", cart.getPlanAmount()), String.format("总注数: \t%d\t 注,", cart.getPlanNotes()));
    }

    @Override
    public void onResume() {
        super.onResume();
        ptrFrameLayout.autoRefresh();
        parameter();
    }

    @OnClick(android.R.id.home)
    public void finish() {
        getActivity().finish();
    }

    @OnClick(R.id.add_menu)
    public void addMune() {
        showBubble();
    }

    @OnClick({R.id.select_mode,R.id.setting_mode})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_mode:
                showBubble();
            case R.id.setting_mode:
                showBubble();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timingView != null) {
            timingView.stop();
        }
    }

    @OnClick(R.id.lottery_shopping_buy)
    public void total() {
        if (timingView.getIssue() == null || timingView.getIssue().length() <= 0) {
            tipDialog("温馨提示", "请稍等，正在加载销售奖期信息……", 0);
            return;
        }
        // ①判断：购物车中是否有投注
        if (!cart.isEmpty()) {
            // ②判断：用户是否登录——被动登录
            if (userCentre.isLogin()) {
                // ③判断：用户的余额是否满足投注需求
                if (cart.getPlanAmount() <= Double.valueOf(userCentre.getUserInfo().getAbalance())) {
                    // ④界面跳转：跳转到追期和倍投的设置界面

                    DanTiaoUtils danTiaoUtils=new DanTiaoUtils();
                    String danTiaoString=danTiaoUtils.isShowDialog(lottery.getSeriesId());

                    if(TextUtils.isEmpty(danTiaoString)){
                        verificationData();
                    }else{
                        tipDialog2("提示",danTiaoString,  TRACK_TURNED_PAGE_DANTIAO);
                        return;
                    }

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
     * 数据验证
     */
    private void verificationData() {
        String msg;
        if (cart.getTraceNumber() > 0) {
            if (cart.getOrdersMap().size() == 0) {
                tipDialog("温馨提示", "请稍等，网速有点慢", 0);
                return;
            }
        }

        String planAmount = String.format("%.3f", cart.getPlanAmount());

        if (cart.getTraceNumber() > 0) {
            msg = getActivity().getResources().getString(R.string.is_shopping_list_chasetip);
            msg = StringUtils.replaceEach(msg, new String[]{"ISSUE", "NOTE", "UNIT", "DOUBLE", "CHASENUM", "MONEY"},
                    new String[]{timingView.getIssue(),
                            String.valueOf(cart.getPlanNotes()),
                            cart.getLucreMode().getName(),
                            String.valueOf(cart.getMultiple()),
                            String.valueOf(cart.getTraceNumber()),
                            planAmount});
        } else {
            msg = getActivity().getResources().getString(R.string.is_shopping_list_tip);
            msg = StringUtils.replaceEach(msg, new String[]{"ISSUE", "NOTE", "UNIT", "DOUBLE", "MONEY"},
                    new String[]{timingView.getIssue(),
                            String.valueOf(cart.getPlanNotes()),
                            cart.getLucreMode().getName(),
                            String.valueOf(cart.getMultiple()),
                            planAmount});
        }

        BetData betData = new BetData();
        betData.setGameId(lottery.getId());
        betData.setIsTrace(0);
        betData.setTraceWinStop(cart.isStopOnWin());
        betData.setBalls(cart.getCodeData());
        betData.setOrders(cart.getOrdersMap());
        betData.setAmount(cart.getPlanAmount());

        try {
            String bet = GsonHelper.toJson(betData).toString();
            PayMoneyCommand payMoneyCommand = new PayMoneyCommand();
//            String sign = SignUtils.delSignMd5HexURLEncoder(payMoneyCommand.getClass());
            payMoneyCommand.setBetData(bet);
            //{"amount":90.0,"balls":[{"extra":{"position":"234","seat":"3"},"ball":"0123456789","jsId":1,"moneyunit":0.5,"multiple":1,"num":90,"prizeGroup":1956,"wayId":181}],"gameId":1,"isTrace":0,"orders":{"181018057":1},"traceWinStop":1}
            payMoneyCommand.setSign(DigestUtils.md5Hex(URLEncoder.encode("action=bet&betdata=" + bet + "&packet=Game&token=" + userCentre.getUserInfo().getToken(), "UTF-8") + userCentre.getKeyApiKey()));

            if (!timingView.getIssue().isEmpty()) {
                CustomDialog.Builder builder = new CustomDialog.Builder(getContext());
                builder.setMessage(msg);
                builder.setTitle("温馨提示");
                builder.setLayoutSet(DialogLayout.SINGLE);
                builder.setPositiveButton("确认投注", (dialog, which) -> {
                    isInTraceState = cart.getTraceNumber() > 0;
                    shopping_buy.setEnabled(false);
                    unusualInfo = ConstantInformation.gatherInfo(getActivity(), payMoneyCommand);
                    ConstantInformation.setLotteryEmblem(lottery);
                    loadBetting(payMoneyCommand);
                    dialog.dismiss();
                });
                builder.create().show();
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    /**
     * 投注成功提示窗
     *
     * @param orderFind
     */
    private void receiptOrderDialog(final OrderFind orderFind) {
        CustomDialog.Builder builder = new CustomDialog.Builder(getContext());
        builder.setMessage(orderFind.getId() > 0 ? "投注成功" : "投注失败!请重新投注");
        builder.setTitle(orderFind.getId() > 0 ? "投注成功" : "投注失败");
        builder.setLayoutSet(DialogLayout.UP_AND_DOWN);
        builder.setNegativeButton("查看投注记录", (dialog, which) -> {
            dialog.dismiss();
            if (orderFind.getId() > 0) {
                if (isInTraceState) {
                    Trace trace = new Trace();
                    trace.setId(orderFind.getId());
                    trace.setLotteryId(lottery.getId());
                    BetOrTraceDetailFragment.launch(ShoppingFragment.this, trace);
                } else {
                    Bet bet = new Bet();
                    bet.setId(orderFind.getId());
                    bet.setLotteryId(lottery.getId());
                    BetOrTraceDetailFragment.launch(ShoppingFragment.this, bet);
                }
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
     * 错误参数与成功参数提示窗
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
            if (track == TRACK_TURNED_PAGE_PICK) {
                getActivity().finish();
            }
        });
        builder.create().show();
    }

    private void tipDialog2(String title, String msg, final int track) {
        CustomDialog.Builder builder = new CustomDialog.Builder(getContext());
        builder.setMessage(msg);
        builder.setTitle(title);
        builder.setLayoutSet(DialogLayout.LEFT_AND_RIGHT);
        builder.setNegativeButton("取消", (dialog, which) ->
        {
            dialog.dismiss();
        });
        builder.setPositiveButton("确认", (dialog, which) ->
        {
            dialog.dismiss();
            verificationData();
        });
        builder.create().show();
    }
    /**
     * 投注请求
     *
     * @param command
     */
    private void loadBetting(PayMoneyCommand command) {
        TypeToken typeToken = new TypeToken<RestResponse<OrderFind>>() {
        };
        RestRequest restRequest = RestRequestManager.createRequest(getActivity(), command, typeToken, restCallback, BUY_TRACE_ID, this);
        restRequest.execute();
    }

    /**
     * 追号期数请求
     */
    private void loadTraceIssue() {
        TraceIssueCommand traceListCommand = new TraceIssueCommand();
        traceListCommand.setLotteryId(lottery.getId());
        TypeToken typeToken = new TypeToken<RestResponse<ArrayList<TraceIssue>>>() {
        };
        RestRequest restRequest = RestRequestManager.createRequest(getActivity(), traceListCommand, typeToken, restCallback, BUY_TRACE_ISSUE_ID, this);
        restRequest.execute();
    }

    /**
     * 彩种返点
     */
    private void loadLotteryRebate() {
        RegChildRebateCommand lotteryListCommand = new RegChildRebateCommand();
        lotteryListCommand.setLotteryId(lottery.getId());
        TypeToken typeToken = new TypeToken<RestResponse<RegChildRebate>>() {
        };
        RestRequest restRequest = RestRequestManager.createRequest(getActivity(), lotteryListCommand, typeToken, restCallback, BUY_REBATE_ID, this);
        restRequest.execute();
    }

    private RestCallback restCallback = new RestCallback() {
        @Override
        public boolean onRestComplete(RestRequest request, RestResponse response) {
            switch (request.getId()) {
                case BUY_TRACE_ID:
                    OrderFind orderFind = (OrderFind) response.getData();
                    if (orderFind != null) {
                        cart.clear();
                        executeCommand(new UserInfoCommand(), restCallback, ID_USER_INFO);
                        shopping_buy.setEnabled(true);
                        adapter.notifyDataSetChanged();
                        receiptOrderDialog(orderFind);
                    }
                    break;
                case ID_USER_INFO:
                    UserInfo userInfo = ((UserInfo) response.getData());
                    userCentre.setUserInfo(userInfo);
                    if (userInfo != null) {
                        balanceText.setText(String.format("余额：%.4f", userInfo.getAbalance()));
                    }
                    break;
                case BUY_REBATE_ID:
                    rebate = (RegChildRebate) response.getData();
                    if (rebate != null && rebate.getGroups().size() > 0) {
                        WangCaiApp.getUserCentre().setPrizeGroup(Integer.parseInt(rebate.getGroups().get(rebate.getGroups().size() - 1)));
                    }
                    addMenu.setEnabled(true);
                    updatePlan();
                    break;
                case BUY_TRACE_ISSUE_ID:
                    ShoppingCart.getInstance().addTraceIssue((ArrayList<TraceIssue>) response.getData());
                    break;
                case GET_SINGLE_PICK:

                    String jsonString= ((JsonString) response.getData()).getJson();
                    try {
                        JSONObject jsonObject= new JSONObject(jsonString);

                        ConstantInformation.is_white=jsonObject.getBoolean("is_white");

                        JSONObject jsonObject2=jsonObject.getJSONObject("sigle_pick");

                        ConstantInformation.sigle_pick=jsonObject2.toString();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            return true;
        }

        @Override
        public boolean onRestError(RestRequest request, int errCode, String errDesc) {
            shopping_buy.setEnabled(true);
            if (errCode == 3004 || errCode == 2016) {
                signOutDialog(getActivity(), errCode);
                return true;
            }
            if (errCode == 2220) {
                showToast(errDesc, Toast.LENGTH_LONG);
                MobclickAgent.reportError(getActivity(), unusualInfo);
                return true;
            } else {
                showToast(errDesc, Toast.LENGTH_LONG);
            }
            return false;
        }

        @Override
        public void onRestStateChanged(RestRequest request, @RestRequest.RestState int state) {
            if (state == RestRequest.RUNNING && request.getId() == BUY_TRACE_ID) {
                dialogShow("进行中...");
            } else {
                dialogHide();
            }
        }
    };

}
