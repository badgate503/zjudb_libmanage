<template>

  <div>
    <div class="main" style="overflow-y: hidden; ">
      <el-container >
        <el-header class="title">
          <div style="margin-top: 12px; display: inline-block;">
            <img  src="./components/icons/logo.svg"
              style=" margin-right: 20px; height: 40px;vertical-align: middle;" />
            <span style="font-size: larger; font-family: 'Georgia';
                color: black; font-weight: bold">Library Management System</span>
            <span style="margin-left: 40px; color:rgba(0, 0, 0, 0.6)" ><a @click="jump">LiuJX</a> @ Zhejiang University</span>
          </div>
        </el-header>
        <el-container style="width: 100%; ">
          <el-aside class="aside" @click="onCollapse=false">
            <el-menu :collapse="onCollapse" class = "lmenu" active-text-color="#E54B3C" background-color="#FFFFFF" default-active="1" text-color="#777777"
               :router="true">
              <el-menu-item index="book">
                <el-icon>
                  <Reading />
                </el-icon>
                <span>图书管理</span>
              </el-menu-item>
              <el-menu-item index="card">
                <el-icon>
                  <Postcard />
                </el-icon>
                <span>借书证管理</span>
              </el-menu-item>
              <el-menu-item index="borrow">
                <el-icon>
                  <Tickets />
                </el-icon>
                <span>借书记录查询</span>
              </el-menu-item>

            </el-menu>
          </el-aside>
          <div class="right" @click="onCollapse = true">
            <el-main style="height: 100%;  width: 80%">
                <RouterView class="content" style="height: 90vh; max-height: 100%; background-color: white; color: black;" />
            </el-main>
          </div>

        </el-container>
      </el-container>
    </div>
  </div>



</template>

<script>
import { RouterView } from 'vue-router'
import "@/assets/App.css"
export default {
  data(){
    return {
      onCollapse: false,
    }
  },
  methods:{
    jump() {
      window.open("http://www.404blog.top");
    }
  }
}
</script>
<script setup>
import { onMounted } from 'vue'

onMounted(() => {
  document.body.style.setProperty('--el-color-primary', '#E54B3C');
  document.body.style.setProperty('--el-color-primary-light-9', '#fdf5f4');
  document.body.style.setProperty('--el-color-primary-light-3', '#E54B3C');
})
</script>

<style scoped>
#app {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background-color: #FFFFFF;
  width: 100vw;
  height: 100vh;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  width: 100%;
  min-height: 100%;
  height: auto;
  background-color: #FFFFFF;

}

.title {
  background-color: #ffffff;
  padding-top: 7px;
  height: 60px;
}
.title div{
  margin-left: 10px;
}
/deep/.el-menu .el-menu-item:hover{
  outline: 0 ;
  color: #E54B3C ;
  background: linear-gradient(270deg, #fbf8f7 0%, #FEFEFE 100%);
}
.aside {
  min-height: calc(100vh - 60px);
  width: fit-content;
  padding-top: 15px;
  background-color: #FFFFFF;
}

</style>
