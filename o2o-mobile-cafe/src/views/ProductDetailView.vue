<template>
    <div>
        <div id="center"> 상품 평점 </div>
        <b-container class="bv-example-row">
            <b-row  class="text-center">
                <b-col >
                    <img id="pictureSize" :src="require(`@/assets/menu/${product.img}`)">
                </b-col>
                <b-col id="colFontSize" cols="8">
                    <b-list-group>
                        <b-list-group-item>상품명 : {{product.name}}</b-list-group-item>
                        <b-list-group-item>상품단가 : {{ product.price}} 원</b-list-group-item>
                        <b-list-group-item>총주문수량 : {{ this.totalOrderCount }}</b-list-group-item>
                        <b-list-group-item>평가 횟수 : {{commentCnt}} </b-list-group-item>
                        <b-list-group-item>평균 평점 : {{commentRate}}</b-list-group-item>
                    </b-list-group>
                </b-col>
            </b-row>
            <br><br>
            <div>
                <b-button id="rightandmargin" variant="success" v-b-modal.modal-prevent-closing>한줄평 남기기</b-button>
                <b-modal
                    id="modal-prevent-closing"
                    ref="modal"
                    title="한줄 평 남기기"
                    @show="resetModal"
                    @hidden="resetModal"
                    @ok="handleOk"
                    >
                    <form ref="form" @submit.stop.prevent="handleSubmit">
                        <b-form-group label="평점" label-for="name-input" invalid-feedback="평점 is required" :state="nameState">
                            <b-form-input id="name-input" v-model="name" :state="nameState" required></b-form-input>
                        </b-form-group>
                        <b-form-group label="한줄 평" label-for="name2-input" :state="name2State">
                            <b-form-input id="name2-input" v-model="name2" :state="name2State" required></b-form-input>
                        </b-form-group>
                    </form>
                </b-modal>
            </div>
            <br><br><br>
            <div >자신이 남긴 평가만 수정, 삭제할 수 있습니다.</div>
            <table class="table table-striped" > 
                <thead >
                    <tr>
                        <th>사용자 명</th>
                        <th>평점</th>
                        <th>한줄평</th>
                    </tr>
                </thead>
                    <tbody>
                    <tr v-for="(comentin, idx) in comments" :key="idx">
                        <td>{{comentin.userId}}</td>
                        <td>{{comentin.rating}}</td>
                        <row>
                        <td>{{comentin.comment}}</td>
                        <b-button class="right" variant="primary">자기꺼면 삭제</b-button>                    
                        <b-button class="right" variant="success">자기꺼면 수정</b-button>
                        </row>
                    </tr>
                    </tbody>
            </table> 
      </b-container>
        
    </div>
</template>

<script>
export default {
    name: "product-detail-view", 
    props: {
        idx: {
            type: Number,
            default: 0,
        },
        user: {
            type: String,
            default: "",
        },
    },
    data(){
        return{
            product: {},
            
            totalOrderCount: 0,
            evaluationCount: 0,
            averageEvaluationScore: 0,
              name: '',
              nameState: null,
              submittedNames: [],
              name2: '',
              name2State: null,
              submittedNames2: [],
              commentsScore: [],
              comments: [],
              user2: {},
              commentCnt: 0,
              commentRate: 0
        }
    },
    methods: {
      checkFormValidity() {
        const valid = this.$refs.form.checkValidity()
        this.nameState = valid
        return valid
      },
      resetModal() {
        this.name = ''
        this.nameState = null
        this.name2= ''
        this.name2State = null
      },
      handleOk(bvModalEvt) {
        // Prevent modal from closing
        bvModalEvt.preventDefault()
        // Trigger submit handler
        this.handleSubmit()
      },
      handleSubmit() {
        // Exit when the form isn't valid
        if (!this.checkFormValidity()) {
          return
        }
        // Push the name to submitted names
        this.submittedNames.push(this.name)
        this.submittedNames2.push(this.name2)
        let commentNew = {}
        commentNew.score=`${this.name}`
        commentNew.comment=`${this.name2}`
        commentNew.user =`${this.user2.name}`
        commentNew.product=`${this.product.id}`


        var commentNew2 = JSON.stringify(commentNew);

        this.$store.dispatch('storeComments', `${commentNew2}`);
        
        let cem = this.$store.getters.getComments;
        let cemlg = cem.length
        let thisComment = [];

        for(let q = 0; q < cemlg; q++) {
            let tojC = cem[q]
            let jC = JSON.parse(tojC)
            thisComment.push(jC)
        }

        console.log(thisComment)

        let commentNums = 0;
        let commentRate = 0;
        for(let j = 0; j < cemlg; j++) {
            if(thisComment[j].user === this.user2.name) {
                    this.comments.push(thisComment[j]);
                    commentNums++
                    commentRate += Number(thisComment[j].score)
            }
          
        }

        console.log(commentRate)

        if(commentNums == 0) {
             commentRate = 0;

        } else {
             commentRate = commentRate /commentNums
        }

        this.commentCnt = commentNums
        this.commnetRate = commentRate


        // Hide the modal manually
        this.$nextTick(() => {
          this.$bvModal.hide('modal-prevent-closing')
        })
      }
    },
    components() {

    },
    async created() {
        
        let products = this.$store.getters.getProducts;
        this.product = products[this.idx];
        this.productImg = this.product.img;


        await this.$store.dispatch('productCntById', this.idx+1);
        let totalCnt = this.$store.getters.getTotalCnt;
        this.totalOrderCount = totalCnt;

        this.user2 = this.$store.getters.getLoginUser;




        this.$store.dispatch('getComments', this.idx);

        let comments = this.$store.getters.getComments;
     
        let cmlegth = comments.length;

        let commentNums = cmlegth;
        let commentRate1 = 0;
        for(let j = 0; j < cmlegth; j++) {
            commentRate1 += comments[j].rating;
        }

        if(commentNums == 0) {
             commentRate1 = 0;
        } else {
             commentRate1 = commentRate1 /commentNums
        }

        this.comments = comments
        this.commentCnt = commentNums
        this.commentRate = commentRate1
    }
};
</script>

<style scoped>
#center{
    text-align: center;
    font-size: 45px;
}
#pictureSize{
    width: 270px;
    height: 400px;
}
#colFontSize{
    text-align: center;
    margin-top: 30px;
    font-size: 30px;
}
#rightandmargin{
      float: right;
      margin-bottom: 20px;
}
.right{
    float: right;
}
</style>