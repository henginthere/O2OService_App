<template>
    <div>
        <!-- variant : 색상 설정 -->
        <b-navbar toggleable="lg" type="dark" variant="info">
            <b-navbar-brand @click="movePage('/')">
                <img id="logo" src="@/assets/logo.png"/>
                SSAFY Cafe
            </b-navbar-brand>

            <b-navbar-nav class="ml-auto" v-show="!loginUser.id">
                <!--movePage를 클릭해서 페이지 이동!-->
                <b-button size="sm" type="button" variant="primary" @click="movePage('/login')">로그인</b-button>
                <b-button size="sm" type="button" variant="warning" @click="movePage('/register')">회원가입</b-button>
            </b-navbar-nav>

            <b-navbar-nav class="ml-auto" v-show="loginUser.id">
                <!--movePage를 클릭해서 페이지 이동!-->
                <b-button size="sm" type="button" variant="primary" @click="movePage('/user-info')">{{loginUser.name}}</b-button>
                <b-button size="sm" type="button" variant="warning" @click="logout">로그아웃</b-button>
            </b-navbar-nav>
        </b-navbar>
    </div>
</template>

<script>
export default {
    name: 'header-nav',
    methods:{
        movePage(url){
            this.$router.push(url);
        },
        async logout(){
            this.$store.dispatch('resetLoginUser');
            console.log(`this.$router : ${window.location.href}`);
        //이슈 있음 
            this.$router.go();
            
                
        }
    },
    computed: {
        loginUser(){
            let loginUser = this.$store.getters.getLoginUser;
            return loginUser;
        }
    }
}
</script>

<!--scope 달면 headerNav 내에서만 사용할 수 있음 -->
<style scope>

#logo {
    width: 30px;
    height: 30px;
}
</style>