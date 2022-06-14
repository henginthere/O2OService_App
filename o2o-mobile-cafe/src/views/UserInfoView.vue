<template>
    <div>
        <b-container>
            <b-alert id="notice" show variant="success">
                <div>우리 {{this.loginUser.name}}회원님은요~~</div>
                <hr id="lineStyle">
                <div>로그인 할 때 아이디는 {{loginUser.id}}을 사용합니다.</div>
                <div>현재 모은 스탬프는 총 {{this.stampCnt}}개로 {{this.curLevel}}입니다.</div>
                <div>앞으로 (개수)만 더 모으면 다음 단계입니다!~~~~~</div>
            </b-alert>

           <hr id="lineStyle">
           <b-list-group >
                <b-list-group-item id=grayColor>이제까지 주문 내역은 아래와 같습니다.</b-list-group-item>
                <b-list-group-item>
                    <div id='listgroup'> 주문 정보를 클릭하면 주문 내역을 살펴볼 수 있습니다. </div>
                    <b-list-group  v-for="(date, idx) in orderDates" v-bind:value=idx :key="idx">
                        <b-list-group-item @click="showOrderDetail(idx)" button>{{ date | yyyyMMdd}}에 웹에서 주문</b-list-group-item>
                         <b-list-group-item v-show="isOpen[idx]" >
                             <order-detail-view v-bind:orderValue="idx" name="order-detail-view"/>
                         </b-list-group-item>
                        <!-- <router-view name="order-detail-view" class="mb-2" /> -->
                    </b-list-group>
                </b-list-group-item>
            </b-list-group>
        </b-container>
    </div>
</template>

<script>
import OrderDetailView from './OrderDetailView.vue';

export default{
    name: "user-info-view",
  components: { 
      OrderDetailView
    },
    data(){
        return{
            loginUser: {},
            isOpen: [false,false,false,false,false,false,false,false,false,false,false],
            
            stampCnt: 0,
            remainStamp: 0,
            curLevel:''
        }
    },
    computed: {
        orderDates() {
            return this.$store.getters.getOrderDates;
        },
        
    },
    mounted() {
        this.loginUser = this.$store.getters.getLoginUser;

        //스탬프 반환
        let order = this.$store.getters.getOrderDetails;
        this.orderList = order;
        console.log(this.orderList);
        console.log(this.orderList[0][0].id);
        
        for(var i = 0; i<this.orderList.length; i++){
            for(var j = 0; j<this.orderList[i].length; j++){
                this.stampCnt += Number(this.orderList[i][j].orderCnts);
            }
        }

        this.findLevel();
    },
    filters : {  
        // filter로 쓸 filter ID 지정
        yyyyMMdd : function(value){ 
            // 들어오는 value 값이 공백이면 그냥 공백으로 돌려줌
            if(value == '') return '';
        
            // 현재 Date 혹은 DateTime 데이터를 javaScript date 타입화
            var js_date = new Date(value);

            // 연도, 월, 일 추출
            var year = js_date.getFullYear();
            var month = js_date.getMonth() + 1;
            var day = js_date.getDate();
            var hour = js_date.getHours();
            var minutes = js_date.getMinutes();

            // 월, 일의 경우 한자리 수 값이 있기 때문에 공백에 0 처리
            if(month < 10){
                month = '0' + month;
            }

            if(day < 10){
                day = '0' + day;
            }

            // 최종 포맷 (ex - '2021-10-08')
            return year + '-' + month + '-' + day + "  " + hour + "시 " + minutes + "분";
        },
    },
    methods: {
        showOrderDetail(idx) {
                if(this.isOpen[idx]==false){
                    this.$set(this.isOpen,idx,true);
                }else{
                    this.$set(this.isOpen,idx,false);
                }
            }
       ,
        findLevel(){
            if(this.stampCnt == 0){
                this.curLevel = "나무";
            }
            else if(1<= this.stampCnt <= 50){
                if(this.stampCnt<=10){
                    this.curLevel = "씨앗1단계";
                }else if(this.stampCnt<=20){
                    this.curLevel = "씨앗2단계";
                }else if(this.stampCnt<=30){
                    this.curLevel = "씨앗3단계";
                }else if(this.stampCnt<=40){
                    this.curLevel = "씨앗4단계";
                }else if(this.stampCnt<=50){
                    this.curLevel = "씨앗5단계";
                }
            }
            else if(51<= this.stampCnt <= 125){
                if(this.stampCnt<=65){
                    this.curLevel = "꽃1단계";
                }else if(this.stampCnt<=85){
                    this.curLevel = "꽃2단계";
                }else if(this.stampCnt<=95){
                    this.curLevel = "꽃3단계";
                }else if(this.stampCnt<=110){
                    this.curLevel = "꽃4단계";
                }else if(this.stampCnt<=125){
                    this.curLevel = "꽃5단계";
                }
            }
            else if(126<= this.stampCnt <= 225){
                if(this.stampCnt<=145){
                    this.curLevel = "열매1단계";
                }else if(this.stampCnt<=165){
                    this.curLevel = "열매2단계";
                }else if(this.stampCnt<=185){
                    this.curLevel = "열매3단계";
                }else if(this.stampCnt<=205){
                    this.curLevel = "열매4단계";
                }else if(this.stampCnt<=225){
                    this.curLevel = "열매5단계";
                }
            }
            else if(226<= this.stampCnt <= 350){
                if(this.stampCnt<=250){
                    this.curLevel = "커피콩1단계";
                }else if(this.stampCnt<=275){
                    this.curLevel = "커피콩2단계";
                }else if(this.stampCnt<=300){
                    this.curLevel = "커피콩3단계";
                }else if(this.stampCnt<=325){
                    this.curLevel = "커피콩4단계";
                }else if(this.stampCnt<=350){
                    this.curLevel = "커피콩5단계";
                }
            }
        },
    }
};
</script>

<style scoped>
#lineStyle{
    height:2px;
    color:green;
    background-color:lightgray;
}
#grayColor{
    background: lightgray;
}
#notice{
    margin-top: 15px;
}
#listgroup{
    padding-top: 9px;
    padding-bottom: 20px;
}
</style>
