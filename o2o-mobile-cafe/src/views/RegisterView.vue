<template>
    <b-container>
        <h1>회원가입</h1>
        <b-form>
            <b-form-group :label="'아이디:'" label-for="input-1">
                <!--v-model 에서 form.id 로 값 저장 -->
                <b-form-input id="input-1" v-model="form.id" type="text" required></b-form-input>
                <span class="error" v-show="isNotRegister">다른 아이디를 사용해주세요!</span>
            </b-form-group>

            <b-form-group :label="'이름:'" label-for="input-2">
                <b-form-input id="input-2" v-model="form.name" type="text" required></b-form-input>
            </b-form-group>

            <b-form-group :label="'비밀번호:'" label-for="input-3">
                <b-form-input id="input-3" v-model="form.pass" type="password" required></b-form-input>
            </b-form-group>            
        </b-form>

        <div>
            <b-button variant="primary" @click="register">가입</b-button>
            <b-button variant="danger" @click="reset">취소</b-button>
        </div>
    </b-container>
</template>

<script>
export default {
    name:'register-view',
    data(){
        return {
            form: {
                id:'',
                name:'',
                pass:''
            },
            tryLogin: false
        }
    },
    computed: {
        user: function() {
            return  this.$store.getters.getUser;
        },
        // isNotRegister: function() {

        //     var alertMulti = false;
        
        //     if(this.user == 1) {
        //         alertMulti = false;
        //         alert("회원가입 성공")
        //         this.reset();
        //         this.movePage("/");
        //     }else if(this.tryLogin == true && this.user == 0) {
        //         alert("중복된 아이디가 존재합니다.")
        //         alertMulti =  true;
        //         this.reset();
        //     }

        //     return alertMulti;
        // }


    },
    created() {
        this.$store.state.user = -1;

    },
    methods: {
        register(){
            if(!this.form.id){
                alert("아이디를 입력하세요.");
                return;
            }
            if(!this.form.name){
                alert("이름을 입력하세요.");
                return;
            }
            if(!this.form.pass){
                alert("비밀번호를 입력하세요.");
                return;
            }
            
            
            this.$store.dispatch("checkUser", this.form.id);
            // alert(this.form.id);
            // alert(this.$store.getters.getIsUser);
            if(this.$store.getters.getIsUser==true){
                alert("아이디가 중복됩니다.");
                this.$store.getters.getIsUser = false;
                return;
            }

            //store에 내용 전달하기 
            //동기로 동작함 -> 비동기가 아닌가 봄!
            this.tryLogin = true;
            this.$store.dispatch("insertUser", this.form);
            alert("회원가입이 완료되었습니다.");
            this.movePage('/');
        },
        reset(){
            this.form.id = '';
            this.form.name = '';
            this.tryLogin = false;
            this.form.pass = '';
            this.$store.state.user = -1;
        },
        movePage(url){
            this.$router.push(url);
        },
    },
}
</script>

<style scoped>
.error {
    color: red;
}
</style>