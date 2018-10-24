package com.wangcai.lottery.util;

import android.text.TextUtils;

import com.wangcai.lottery.app.WangCaiApp;
import com.wangcai.lottery.data.Method;
import com.wangcai.lottery.data.MethodType;
import com.wangcai.lottery.game.Kl12CommonGame;
import com.wangcai.lottery.material.ConstantInformation;
import com.wangcai.lottery.material.ShoppingCart;
import com.wangcai.lottery.material.Ticket;
import com.wangcai.lottery.user.UserCentre;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gan on 2018/6/27.
 * 单挑模式工具类
 */

public class DanTiaoUtils {

    //是否弹出单挑模式 对话框
    public String isShowDialog(int lotterySeriesId){

        if( ConstantInformation.is_white){
            return null;
        }


        List<Ticket> tickets=ShoppingCart.getInstance().getCodesMap();

        //对相同玩法(ticket.getChooseMethod().getId())的注数进行相加start
        List<Ticket> newTicketList = new ArrayList<>();
        List<Integer> hasMethods = new ArrayList<>();
        for(int i=0;i<tickets.size();i++){
            Ticket ticket=tickets.get(i);
            if(hasMethods.contains(ticket.getChooseMethod().getId())){
                continue;
            }
            hasMethods.add(ticket.getChooseMethod().getId());
            long  sumChooseNotes=ticket.getChooseNotes();
            for(int j=i+1;j<tickets.size();j++){
                Ticket ticket2=tickets.get(j);
                if(ticket.getChooseMethod().getId()==ticket2.getChooseMethod().getId()){
                    sumChooseNotes+=ticket2.getChooseNotes();
                }
            }
            //为了防止改变原有的list集合
            Ticket newTicket=new Ticket();
            Method method=new Method();
            method.setNameCn(ticket.getChooseMethod().getNameCn());
            method.setId(ticket.getChooseMethod().getId());
            method.setNameEn(ticket.getChooseMethod().getNameEn());
//            method.setSingle_pick(ticket.getChooseMethod().getSingle_pick());
            newTicket.setChooseMethod(method);

            MethodType  newMethodType=new MethodType();
            newMethodType.setNameCn(ticket.getMethodType().getNameCn());
            newTicket.setMethodType(newMethodType);

            newTicket.setChooseNotes(sumChooseNotes);
            newTicketList.add(newTicket);
        }
        //对相同玩法(ticket.getChooseMethod().getCname())的注数进行相加end

//        switch (lotterySeriesId){
//            case 1://时时彩系列
//                return  sscIsShowDialog(newTicketList);
//            case 2://11选5系列
//                return  isShowDialog11Select5(newTicketList);
//            case 3://3D系列
//                return  isShowDialog3D(newTicketList);
//            case 4://快3系列
//                return  isShowDialogK3(newTicketList);
////            case 8://快乐十二
////            case 9: //快乐十分
////                return  isShowDialogKl(newTicketList);
//        }

        return isShowDialogAll(newTicketList);

    }

    private String isShowDialogAll(List<Ticket> tickets) {
            /*有单挑的玩法名称*/
        StringBuilder hasDanTiaoMethods=new StringBuilder();

        for(int i=0;i<tickets.size();i++) {
            Ticket ticket=tickets.get(i);

            String methodName = "";
            if (ticket.getMethodType() != null) {
                methodName = ticket.getMethodType().getNameCn() + "" + ticket.getChooseMethod().getNameCn();
            } else {
                methodName = ticket.getChooseMethod().getNameCn();
            }

            int single_pick=0;

            //获取single_pick start
            if(!TextUtils.isEmpty(ConstantInformation.sigle_pick)){
                try {
                    JSONObject jsonObject=new  JSONObject(ConstantInformation.sigle_pick);

                    int id=ticket.getChooseMethod().getId();

                    Object  sigle_pick_string=  jsonObject.get(String.valueOf(id));

                    if(sigle_pick_string!=null){
                        single_pick= Integer.parseInt(sigle_pick_string.toString());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //获取single_pick end

            if(ticket.getChooseNotes()<=single_pick){
                hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
            }
        }
        if(!TextUtils.isEmpty(hasDanTiaoMethods)){
            return hasDanTiaoMethods.append("投注含单挑注单，单挑注单盈利上限为3万元，是否继续投注？").toString();
        }
        return null;
    }

//    private String isShowDialogKl(List<Ticket> tickets) {
//            /*有单挑的玩法名称*/
//        StringBuilder hasDanTiaoMethods=new StringBuilder();
//
//        for(int i=0;i<tickets.size();i++) {
//            Ticket ticket=tickets.get(i);
//
//            String methodName = "";
//            if (ticket.getMethodType() != null) {
//                methodName = ticket.getMethodType().getNameCn() + "" + ticket.getChooseMethod().getNameCn();
//            } else {
//                methodName = ticket.getChooseMethod().getNameCn();
//            }
//            switch (ticket.getChooseMethod().getId()){
//                case 158://三同号单选
//                    if(ticket.getChooseNotes()<=2){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//            }
//        }
//        if(!TextUtils.isEmpty(hasDanTiaoMethods)){
//            return hasDanTiaoMethods.append("投注含单挑注单，单挑注单盈利上限为3万元，是否继续投注？").toString();
//        }
//        return null;
//    }

    private String isShowDialogK3(List<Ticket> tickets) {
            /*有单挑的玩法名称*/
        StringBuilder hasDanTiaoMethods=new StringBuilder();

        for(int i=0;i<tickets.size();i++) {
            Ticket ticket=tickets.get(i);

            String methodName = "";
            if (ticket.getMethodType() != null) {
                methodName = ticket.getMethodType().getNameCn() + "" + ticket.getChooseMethod().getNameCn();
            } else {
                methodName = ticket.getChooseMethod().getNameCn();
            }
            int single_pick=Integer.parseInt(ticket.getChooseMethod().getSingle_pick());
            if(ticket.getChooseNotes()<=single_pick){
                hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
            }
//            switch (ticket.getChooseMethod().getId()){
//                case 158://三同号单选
//                    if(ticket.getChooseNotes()<=2){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 159:
//                case 160:
//                case 161:
//                case 162:
//                case 163:
//                case 164:
//                case 165:
//                case 166:
//                case 167:
//
//                case 375:
//                case 376:
//                case 377:
//
//                case 378:
//                case 379:
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//            }
        }
        if(!TextUtils.isEmpty(hasDanTiaoMethods)){
            return hasDanTiaoMethods.append("投注含单挑注单，单挑注单盈利上限为3万元，是否继续投注？").toString();
        }
        return null;
    }

    //3D
    private String isShowDialog3D(List<Ticket> tickets) {
           /*有单挑的玩法名称*/
        StringBuilder hasDanTiaoMethods=new StringBuilder();

        for(int i=0;i<tickets.size();i++) {
            Ticket ticket=tickets.get(i);

            String methodName = "";
            if (ticket.getMethodType() != null) {
                methodName = ticket.getMethodType().getNameCn() + "" + ticket.getChooseMethod().getNameCn();
            } else {
                methodName = ticket.getChooseMethod().getNameCn();
            }
            int single_pick=Integer.parseInt(ticket.getChooseMethod().getSingle_pick());
            if(ticket.getChooseNotes()<=single_pick){
                hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
            }
//            switch (ticket.getChooseMethod().getId()){
//                /*① 三星*/
//                case 136:
//                case 123:
//                case 139:
//                    if(ticket.getChooseNotes()<=10){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 131:
//                case 132:
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 140://组选和值
//                    if(ticket.getChooseNotes()<=2){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//               /*① 二星*/
//                case 138:
//                case 128:
//                case 137:
//                case 126:
//                case 135:
//                case 129:
//                case 134:
//                case 127:
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                /*① 一星*/
//                case 563:
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                /*① 不定位*/
//                case 133:
//                case 485:
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                /*① 龙虎和*/
//                case 372:
//                case 373:
//                case 374:
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                /*① 猜不出*/
//                case 367:
//                case 368:
//                case 369:
//                case 370:
//                case 371:
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//            }
        }
        if(!TextUtils.isEmpty(hasDanTiaoMethods)){
            return hasDanTiaoMethods.append("投注含单挑注单，单挑注单盈利上限为3万元，是否继续投注？").toString();
        }
        return null;
    }

    //11选5
    private String isShowDialog11Select5(List<Ticket> tickets) {
        /*有单挑的玩法名称*/
        StringBuilder hasDanTiaoMethods=new StringBuilder();

        for(int i=0;i<tickets.size();i++) {
            Ticket ticket=tickets.get(i);
            String methodName = "";
            if (ticket.getMethodType() != null) {
                methodName = ticket.getMethodType().getNameCn() + "" + ticket.getChooseMethod().getNameCn();
            } else {
                methodName = ticket.getChooseMethod().getNameCn();
            }
            int single_pick=Integer.parseInt(ticket.getChooseMethod().getSingle_pick());
            if(ticket.getChooseNotes()<=single_pick){
                hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
            }

//            switch (ticket.getChooseMethod().getId()){
//                /*① 三码*/
//                case 112:
//                case 95:
//                    if(ticket.getChooseNotes()<=9){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 108:
//                case 97:
//                case 121://前三组选胆拖
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                /*② 二码*/
//                case 111:
//                case 94:
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 107:
//                case 96:
//                case 120://前二组选胆拖
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                 /*③ 任选复式*/
//                case 98://任选复式、单式一中一
//                case 99://任选复式、单式、胆拖二中二
//                case 100://任选复式、单式、胆拖三中三
//                case 105://任选复式、单式、胆拖八中五
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 101://任选复式、单式、胆拖四中四
//                case 104://任选复式、单式、胆拖七中五
//                    if(ticket.getChooseNotes()<=3){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 102://任选复式、单式、胆拖五中五
//                case 103://任选复式、单式、胆拖六中五
//                    if(ticket.getChooseNotes()<=4){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                 /*③ 任选单式*/
//                case 86://任选复式、单式一中一
//                case 87://任选复式、单式、胆拖二中二
//                case 88://任选复式、单式、胆拖三中三
//                case 93://任选复式、单式、胆拖八中五
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 89://任选复式、单式、胆拖四中四
//                case 92://任选复式、单式、胆拖七中五
//                    if(ticket.getChooseNotes()<=3){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 90://任选复式、单式、胆拖五中五
//                case 91://任选复式、单式、胆拖六中五
//                    if(ticket.getChooseNotes()<=4){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                 /*③ 任选胆拖*/
//                case 113://任选复式、单式、胆拖二中二
//                case 114://任选复式、单式、胆拖三中三
//                case 119://任选复式、单式、胆拖八中五
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 115://任选复式、单式、胆拖四中四
//                case 118://任选复式、单式、胆拖七中五
//                    if(ticket.getChooseNotes()<=3){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 116://任选复式、单式、胆拖五中五
//                case 117://任选复式、单式、胆拖六中五
//                    if(ticket.getChooseNotes()<=4){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                /*⑤ 猜不出*/
//                case 380:
//                case 381:
//                case 382:
//                case 383:
//                case 384:
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                default:
//                    ;
//            }
        }
        if(!TextUtils.isEmpty(hasDanTiaoMethods)){
            return hasDanTiaoMethods.append("投注含单挑注单，单挑注单盈利上限为3万元，是否继续投注？").toString();
        }
        return null;
    }

    //时时彩是否弹出单挑模式 对话框
    private String sscIsShowDialog(List<Ticket> tickets) {
        /*有单挑的玩法名称*/
        StringBuilder hasDanTiaoMethods=new StringBuilder();

        for(int i=0;i<tickets.size();i++){
            Ticket ticket=tickets.get(i);
            //method.getNameEn() + method.getId()
            String methodName = "";
            if (ticket.getMethodType() != null) {
                methodName = ticket.getMethodType().getNameCn() + "" + ticket.getChooseMethod().getNameCn();
            } else {
                methodName = ticket.getChooseMethod().getNameCn();
            }
            int single_pick=Integer.parseInt(ticket.getChooseMethod().getSingle_pick());
            if(ticket.getChooseNotes()<=single_pick){
                hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
            }
//            switch (ticket.getChooseMethod().getId()){
//                /*① 五星*/
//                case 68://五星 直选复式
//                case 7://五星 直选单式
//                    if(ticket.getChooseNotes()<=single_pick){
////                  if(ticket.getChooseNotes()<=1000){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                /*② 前四*/
//                case 295://直选复式
//                case 351://直选单式
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=100){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                 /*③ 后四*/
//                case 67://直选复式
//                case 6://直选单式
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=100){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                /*④ 前三*/
//                case 65://直选复式
//                case 1://直选单式
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=10){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 71://直选和值
//                case 60://直选跨度
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=10){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 16://组三
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 17://组六
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=2){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 13://混合组选
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 75://组选和值
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=2){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 2://组三单式
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=3){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 3://组六单式
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=2){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 33://和值尾数
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 48://特殊号码
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//
//                 /*⑤ 中三*/
//                case 150://直选复式
//                case 142://直选单式
//                    if(ticket.getChooseNotes()<=10){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 151://直选和值
//                case 149://直选跨度
//                    if(ticket.getChooseNotes()<=10){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 145://组三
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 146://组六
//                    if(ticket.getChooseNotes()<=2){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 152://混合组选
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 154://组选和值
//                    if(ticket.getChooseNotes()<=2){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 143://组三单式
//                    if(ticket.getChooseNotes()<=3){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 144://组六单式
//                    if(ticket.getChooseNotes()<=2){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 156://和值尾数
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 155://特殊号码
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//
//                /*⑥ 后三*/
//                case 69://直选复式
//                case 8://直选单式
//                    if(ticket.getChooseNotes()<=10){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 73://直选和值
//                case 62://直选跨度
//                    if(ticket.getChooseNotes()<=10){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 49://组三
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 50://组六
//                    if(ticket.getChooseNotes()<=2){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 81://混合组选
//                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 80://组选和值
//                    if(ticket.getChooseNotes()<=2){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 9://组三单式
//                    if(ticket.getChooseNotes()<=3){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 10://组六单式
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=2){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 54://和值尾数
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 57://特殊号码
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                /*⑥ 二星*/
//                /* 直选*/
//                case 70://后二复式
//                case 11://后二单式
//                case 74://后二和值
//                case 63://后二跨度
//
//                case 66://前二复式
//                case 4://前二单式
//                case 72://前二和值
//                case 61://前二跨度
//
//                /*组选*/
//                case 59://后二复式
//                case 12://后二单式
//                case 77://后二和值
//
//                case 20://前二复式
//                case 5://前二单式
//                case 76://前二和值
//
//                /* 一星*/
//                /* 定位胆*/
//                case 78://定位胆
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//
//                /*⑦不定位*/
//                case 51://后三一码不定位
//                case 52://后三二码不定位
//                case 18://前三一码不定位
//                case 21://前三二码不定位
//
//                case 34://四星一码不定位
//                case 35://四星二码不定位
//
//                case 36://五星二码不定位
//                case 37://五星三码不定位
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//
//                /*⑧大小单双*/
//                case 58:
//                case 53:
//                case 19:
//                case 22:
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//
//                /*⑨趣味*/
//                case 38://五码趣味三星
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=40){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 39://四码趣味三星
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=20){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 55://后三趣味二星
//                case 40://前三趣味二星
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=2){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//
//                case 41://五码区间三星
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=250){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 42://四码区间三星
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=50){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 56://后三区间二星
//                case 43://前三区间二星
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=5){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//
//                case 44://五星二码不定位
//                case 45://五星三码不定位
//                case 46://三星报喜
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                case 47://四季发财
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=20){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//
//                /*⑩龙虎和*/
//                case 352:
//                case 353:
//                case 354:
//                case 355:
//                case 356:
//                case 357:
//                case 358:
//                case 359:
//                case 360:
//                case 361:
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//               /*⑩猜不出*/
//                case 362:
//                case 363:
//                case 364:
//                case 365:
//                    if(ticket.getChooseNotes()<=single_pick){
////                    if(ticket.getChooseNotes()<=1){
//                        hasDanTiaoMethods.append("<font color=\'#8F0000\'>\""+methodName+"\"</font> ");
//                    }
//                    break;
//                default:
//                    ;
//            }
        }

        if(!TextUtils.isEmpty(hasDanTiaoMethods)){
            return hasDanTiaoMethods.append("投注含单挑注单，单挑注单盈利上限为3万元，是否继续投注？").toString();
        }

        return null;
    }

}
