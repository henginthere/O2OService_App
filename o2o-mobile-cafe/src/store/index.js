import Vue from 'vue'
import Vuex from 'vuex'
import http from '@/util/http-common';

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    check_id:false,
    user: {},
    loginUser: {
      id: '',
      name: '',
      pass: '',
      stampList:[],
      stamps: 0
    },
    order:[],
    products: [],
    orderDate: [],
    orderDetails: [],
    orderCnt: 0,
    orderNumsHistory: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9],
    comments: []
  },
  getters: {
    getUser(state) {
      return state.user;
    },
    getLoginUser(state) {
      return state.loginUser;
    },
    getProducts(state) {
      return state.products;
    },
    getOrderDates(state) {
      return state.orderDate;
    },
    getOrderDetails(state) {
      return state.orderDetails;
    },
    getOrderHistory(state) {
      return state.orderNumsHistory;
    },
    getComments(state) {
      return state.comments;
    },
    getTotalCnt(state) {
      return state.orderCnt;
    },
    getIsUser(state) {
      return state.check_id;
    }
  },
  mutations: {
    INSERT_USER(state, user) {
      state.user = user;
    },
    LOGIN_USER(state, loginUser) {
      state.loginUser.id = loginUser.id;
      state.loginUser.name = loginUser.name;
      state.loginUser.pass = loginUser.pass;
      state.loginUser.stampList = loginUser.stampList;
      state.loginUser.stamps = loginUser.stamps;
    },
    SELECT_PRODUCT(state, products) {
      state.products = products;
    },
    STORE_ORDERDATES(state, orderDates) {
      state.orderDate.push(orderDates)
    },
    STORE_ORDERDDETAILS(state, orderDetails) {
      state.orderDetails.push(orderDetails)
    },
    STORE_ORDERHISTORY(state, orderHistory) {
      state.orderHistory = orderHistory;
    },
    STORE_COMMENT(state, comment) {
      state.comments.push(comment);
    },
    STORE_TOTALCNT(state, cnt) {
      state.orderCnt = cnt;
    },
    STORE_COMMENTS(state, comments) {
      state.comments = comments;
    },
    CHECK_USER(state, check_id) {
      state.check_id = check_id;
    },

  },
  actions: {

    checkUser({commit},user_id) {
      http.get(`/user/isUsed?id=${user_id}`)
        .then((response) => {
          var a = response.data;
          console.log("???");
          console.log(a);
          commit('CHECK_USER', response.data);
        })
      .catch((error) => {
        console.log(error);
      });
    },

    insertUser({ commit }, user) {

      http.post('/user', user)
        .then((response) => {
          var a = response.data;
          console.log(a);
        commit("INSERT_USER", response.data);
      })
      .catch((error) => {
        console.log(error);
      });
    },
    async login({ commit }, user) {
      await http.post('/user/login', user)
              .then((loginUser) => {
                console.log(loginUser.data.id);
          commit('LOGIN_USER', loginUser.data);
        })
        .catch((error) => {
          console.log(error);
        });
    },
    resetLoginUser() {
            http.post('/user/logout')
            .then(() => {})
      .catch((error) => {
        console.log(error);
      });
    },
    selectProducts({ commit }) {
      //TODO: 아래 코드는 서버 대신 작성한 코드이므로 추후 삭제
      http.get('/product')
      .then((response) => {
        var a = response.data;
        console.log(a);
        commit('SELECT_PRODUCT', response.data);
      })
    .catch((error) => {
      console.log(error);
    });
    },
    storeOrderDate({ commit }, orderDates) {
      commit('STORE_ORDERDATES', orderDates);
    },
    storeOrderDetail({ commit }, orderDetails) {
      commit('STORE_ORDERDDETAILS', orderDetails);
    },
    storeOrderHistory({ commit }, orderDate) {
      commit('STORE_ORDERHISTORY', orderDate);
      console.log(orderDate);
    },
    storeComments({ commit }, payload) {
      commit('STORE_COMMENT', payload);
    },
    async productCntById({commit}, no) {
          await http.get(`/product/${no}`)
          .then((response) => {
            var a = response.data;
            console.log(a);
          commit("STORE_TOTALCNT", response.data["quantity"]);
        })
        .catch((error) => {
          console.log(error);
        });
    },
    insertOrder( payload) {
      http.post("/order", payload)
          .then((response) => {
            console.log(response);
          })
        .catch((error) => {
          console.log(error);
        });
    },
    getComments({ commit }, idx) {
      http.get(`/comment/list/${idx}`)
          .then((response) => {
            commit("STORE_COMMENTS", response.data);
        })
        .catch((error) => {
          console.log(error);
        });
    },

  },
  modules: {
  }
})
