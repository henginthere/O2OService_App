<template>
    <div>
        <div>주문 상세</div>
               <div v-for="(p, idx) in orderList" :key="idx">
                    <img :src="require('@/assets/menu/'+p.img)" height="50px" alt="이미지"/>
                                 품명 : {{p.name}}, 단가 : {{p.price}}원,  {{p.orderCnts}}잔          
                </div>
 
        <div>총 비용: {{ totalMoney }} 스탬프 적립: {{}} </div>
    </div>

</template>


<script>
export default {
    name: 'order-detail-view',
     props: ["orderValue"],

    data(){
        return {
            orderList:[],
            totalMoney: 0,
            totalStamp: 0,
        }
    },
    mounted(){
        let order = this.$store.getters.getOrderDetails;
        console.log(order);
        this.orderList = order[this.orderValue];
        console.log(this.orderList);
        console.log(this.orderList[0].id);
        
        for(var i = 0; i<this.orderList.length; i++){
            this.totalMoney += this.orderList[i].price * this.orderList[i].orderCnts;
        }
    }

}
</script>