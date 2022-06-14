<template>
    <div>
        <div >
            <b-button id="toggleBt" v-show="loginUser.id" v-b-toggle.sidebar-right @click="showOrderInfo">주문 정보 보기</b-button>
            <b-sidebar id="sidebar-right" title="주문 정보" right shadow  backdrop>
            <div class="px-3 py-2">
                <b-list-group v-for="(p, idx) in orderProducts" :key="idx">
                        <b-list-group-item>
                            <span>
                                <img :src="require('@/assets/menu/'+p.img)" height="100px"  img-top alt="Image">
                            </span>
                            <span>
                                <div>제품명: {{p.name}}</div>
                                <div>개당 가격: {{p.price}}</div>
                                <div>주문량: {{p.orderCnts}}</div>
                            </span>
                        </b-list-group-item>
                </b-list-group>
            <div v-show="!orderProducts[0]" >선택한 제품이 없습니다!</div>
            <b-button id="orderButon" class="right" v-show="orderProducts[0]" variant="success" @click="moveUserInfo">주문</b-button>                    
            </div>
            </b-sidebar>
        </div>
        <b-container id="container">
            <b-alert id="notice" show variant="primary" >
                    <h3>커피</h3>
                    <div>뛰면서 즐기는 커피 한잔의 여유</div>
                    <div>{{new Date() | yyyyMMdd}}</div>
                    <hr>
                    <div>갓 볶은 아라비카산 원두만 고집합니다.</div>
            </b-alert>   
            <b-row>
                <b-col cols="3" v-for="(item, idx) in products" :key="idx" class="text-center">
                        <b-card class=productSize>
                            <b-card-img :src="require('@/assets/menu/'+item.img)" height="300px"  img-top alt="Image"  @click="moveProductDetail(idx)"></b-card-img>
                            <b-card-title :title="item.name"></b-card-title>
                            <b-card-text>{{ item.price }} 원</b-card-text>
                            <b-button class="pdCnt" v-show="loginUser.id" variant="primary" >{{orderNums[idx]}}</b-button>                    
                            <b-button class="pdCnt" v-show="loginUser.id" variant="success" @click="increaseNums(idx)">+</b-button>                    
                            <b-button class="pdCnt" v-show="loginUser.id" variant="dark" @click="decreaseNums(idx)">-</b-button>                    
                            <b-button class="pdCnt" v-show="loginUser.id" variant="warning"><img class="cart" src='@/assets/shopping_cart.png'></b-button>   
                        </b-card>
                </b-col>
            </b-row>
        </b-container>
        <b-button class="awsws"  variant="primary" @click="alertTest(5)">aaa</b-button>                    
    </div>
</template>

<script>

export default {
    name: 'product-list-view',
    created(){
        this.$store.dispatch('selectProducts');
        for(let i = 0; i <this.products.length; i ++) {
            this.orderNums[i] = 0;
        }

        
    },
    data() {
        return {
            orderNums: [],
            orderProducts: [],
            orderDates: []
        }
    },
    
    computed: {
        products(){
            return this.$store.getters.getProducts;
        },
        loginUser(){
            let loginUser = this.$store.getters.getLoginUser;
            return loginUser;
        }
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
        }
    },
    methods: {

        moveProductDetail(idx) {
            let loginUser= this.$store.getters.getLoginUser;
            if(loginUser.id) {
                this.$router.push({name: 'product-detail-view', params: {idx: idx, user: loginUser.id }});
            }
        },
        moveUserInfo() {
            this.$store.dispatch('storeOrderDate', `${this.orderDates}`);
            let a= this.$store.getters.getOrderDates;
           console.log(a);

            this.$store.dispatch('storeOrderDetail', this.orderProducts);
            console.log("storeorderdetail");
            console.log(this.orderProducts);

            let orderHistory = this.$store.getters.getOrderHistory;
            for(let k = 0; k < this.products.length; k++) {
                orderHistory[k] += this.orderNums[k];
            }
            // console.log[orderHistory];
            this.$store.dispatch('storeOrderHistory', `${orderHistory}`);
            let c = this.$store.getters.getOrderHistory;
      
            console.log(c);
            // let vlaue = c[3]; //orderhistry 값 저장
            // console.log(vlaue);//orderhistry 값 출력



            let newOrderList = [];
            for(var i = 0; i<this.orderProducts.length; i++){
                let order = {
                    id: 0,
                    orderId: this.orderDates.length,
                    productId: this.orderProducts.id,
                    quantity: this.orderProducts.orderCnts
                }
                newOrderList[newOrderList.length++]=order;
            }
            
            let loginUser= this.$store.getters.getLoginUser;
        
            // //주문 추가하기 
            // let order = {
            //     completed: "N",
            //     details: this.newOrderList,
            //     id:0,
            //     orderTable: "1",//전체 오더테이블 개수 +1
            //     orderTime: this.orderDates,
            //     userId: loginUser.id
            // }

            //table 일단 하드코딩 
            console.log("loginuser");
            console.log(loginUser.id);

            var order = {
                completed:"N",
                userId: loginUser.id,
                orderTable: "1"
            }
   this.$store.dispatch('insertOrder', order);


            console.log(loginUser);
            this.$router.push({name: 'user-info-view', params: {user: loginUser.id }});
            
        },
        increaseNums(idx) {
            this.orderNums[idx] += 1;
            let arr = this.orderNums.splice(0);
            this.orderNums = arr;
        },
        decreaseNums(idx) {
            if(this.orderNums[idx]===0) {
                return
            }
            this.orderNums[idx] -= 1;
            let arr = this.orderNums.splice(0);
            this.orderNums = arr;
        },
        showOrderInfo() {
            let orderNums = this.orderNums;
            let productInfos = this.products;
            let orderProducts = [];
            let orderDate = [];


            console.log(orderNums);

            for(var i = 0; i < productInfos.length; i ++) {
                if(orderNums[i] > 0) {
                    productInfos[i].orderCnts = `${orderNums[i]}`;
                    orderProducts.push(productInfos[i]);
                }
            }

            orderDate.push(new Date());
            console.log(orderDate);

            this.orderProducts = orderProducts;
            this.orderDates = orderDate;

        }
	}
    
}
</script>

<style scoped>
.productSize{
    width: 244px;
    height: 458px;
    margin: 10px;
    
}

.pdCnt{
    /* width:40px;
    margin: 3px; */
    margin: 3px;
    width: 22%;
    height:40px;
    
}

.cart{
    width: 24px;
    height:24px;
}

#notice {
    margin-top: 20px;
    margin-left: 7px;
}

#toggleBt {
    float: right;
    margin-top: 70px;
    margin-right: 40px;
    height: 120px;
}

#sidebar-right {
    text-align: center;

}

#toggleBt {
      color:black;
      font-size: 15px;
      background-color: #c8d7ee;
      position: fixed;
      width: 100px;
      height: 30%;
      margin-left: 1320px;
      margin-top:0px
}

#container {
    opacity : 1;
}

#orderButon {
    width: 100%;
}
</style>