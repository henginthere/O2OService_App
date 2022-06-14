<template>
    <b-container>
        <h1>로그인</h1>
        <b-form>
            <b-form-group :label="'아이디:'" label-for="input-1">
                <!--v-model 에서 form.id 로 값 저장 -->
                <b-form-input id="input-1" v-model="form.id" type="text" required></b-form-input>
            </b-form-group>

            <b-form-group :label="'비밀번호:'" label-for="input-2">
                <b-form-input id="input-2" v-model="form.pass" type="password" required></b-form-input>
            </b-form-group>            
        </b-form>

        <div>
            <b-button variant="primary" @click="login">로그인</b-button>
            <b-button variant="danger" @click="movePage">취소</b-button>
        </div>
    </b-container>
</template>

<script>
export default {
    data(){
        return{
            form: {
                id: '',
                pass:'',
            }
        }
    },
    methods: {
        async login(){
            await this.$store.dispatch('login',this.form);
            let loginUser = this.$store.getters.getLoginUser;

            //로그인 안된 경우만 경고창 
            if(!loginUser.id){
                alert("로그인 실패입니다. 아이디와 비밀번호를 확인하세요.");
            }
            else{
                this.movePage('/');
            }
        },
        movePage(url){
            this.$router.push(url);
        },
        reset(){
            this.form = {
                id: '',
                pass: ''
            };
        }
    },
}
</script>