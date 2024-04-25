<template>

    <el-scrollbar height="100%" style="width: 100%;">
        <!-- 标题和搜索框 -->
        <div style="margin-top: 20px; margin-left: 40px; font-size: 2em; font-weight: bold; ">借书证管理
            <el-input v-model="toSearch" :prefix-icon="Search" size="large"
                style=" width: 15vw;min-width: 150px; margin-left: 30px; margin-right: 50px; float: right;margin-top:5px;" clearable />
        </div>


        <!-- 借书证卡片显示区 -->
        <div style="display: flex;flex-wrap: wrap; justify-content: start;">
            <el-button class="newCardBox"v-show="newCardBoxVisible"
                       @click="handleAddNew">
              <el-icon style="height: 50px; width: 50px;">
                <Plus style="height: 100%; width: 100%;" />
              </el-icon>
            </el-button>




          <el-card class="cardBox" style="max-width: 600px" v-show="newCardVisible">
            <template #header>
              <div class="card-header">
                New Card
              </div>
            </template>
            <el-row >姓名： <el-input size="small" v-model="newCardInfo.name" style="width: 60%;" clearable /></el-row>
            <el-row style="margin-top: 10px;">部门：<el-input size="small" v-model="newCardInfo.department" style="width: 60%;" clearable /></el-row>
            <el-row style="margin-top: 10px;">类型：<el-select size="small" v-model="newCardInfo.type" style="width: 60%;">
              <el-option v-for="type in types" :key="type.value" :label="type.label" :value="type.value" />
            </el-select></el-row>

            <template #footer>
              <el-button plain   @click="handleCancelAdd"  >取消</el-button>
              <el-button color="#E54B3C"
                         @click="ConfirmNewCard" icon="Check"
                         style="margin-left: 10px;" :disabled="newCardInfo.name.length === 0 || newCardInfo.department.length === 0">确定</el-button>

            </template>
          </el-card>


            <!-- 借书证卡片 -->
          <el-card class="cardBox" style="max-width: 600px" v-for="card in cards" v-show="card.name.includes(toSearch)" :key="card.id">
            <template #header>
              <div class="card-header">
                Card #<el-text size="large" type="primary" tag="b">{{card.id}}</el-text>
              </div>
            </template>
            <div style="width: 100%" v-show="!modifyCardVisible || card.id!==toModifyInfo.id">
              <el-row >姓名：{{card.name}}</el-row>
              <el-row style="margin-top: 10px;">部门：{{card.department}}</el-row>
              <el-row style="margin-top: 10px;">类型：<el-tag>{{card.type}}</el-tag></el-row>
            </div>
            <div style="width: 100%" v-show="modifyCardVisible && card.id===toModifyInfo.id">
              <el-row >姓名： <el-input size="small" v-model="toModifyInfo.name" style="width: 60%;" clearable /></el-row>
              <el-row style="margin-top: 10px;">部门：<el-input size="small" v-model="toModifyInfo.department" style="width: 60%;" clearable /></el-row>
              <el-row style="margin-top: 10px;">类型：<el-select size="small" v-model="toModifyInfo.type" style="width: 60%;">
                <el-option v-for="type in types" :key="type.value" :label="type.label" :value="type.value" />
              </el-select></el-row>
            </div>


            <template #footer>
              <div style="width:100%;" v-show="!modifyCardVisible || card.id!==toModifyInfo.id">
                <el-button plain :icon="Edit"  @click="handleEdit(card)"  >编辑</el-button>
                <el-button plain :icon="Files"  @click="handleRedirect(card.id)"  >历史</el-button>
                <el-button color="#E54B3C" :icon="Delete"
                           @click="this.toRemove = card.id, this.removeCardVisible = true"
                           style="margin-left: 10px;" >删除</el-button>
              </div>
              <div style="width: 100%" v-show="modifyCardVisible && card.id===toModifyInfo.id">
                <el-button plain   @click="modifyCardVisible=false"  >取消</el-button>
                <el-button color="#E54B3C" @click="ConfirmModifyCard" icon="Check"
                           :disabled="toModifyInfo.name.length === 0 || toModifyInfo.department.length === 0"
                           style="margin-left: 10px;" >确定</el-button>
              </div>

            </template>
          </el-card>
          <el-divider/>

        </div>


        <el-dialog :title="'修改信息(借书证ID: ' + this.toModifyInfo.id + ')'" width="30%"
            align-center>
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                姓名：
                <el-input v-model="toModifyInfo.name" style="width: 12.5vw;" clearable />
            </div>
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                部门：
                <el-input v-model="toModifyInfo.department" style="width: 12.5vw;" clearable />
            </div>
            <div style="margin-left: 2vw;   font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                类型：
                <el-select v-model="toModifyInfo.type" size="middle" style="width: 12.5vw;">
                    <el-option v-for="type in types" :key="type.value" :label="type.label" :value="type.value" />
                </el-select>
            </div>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="modifyCardVisible = false">取消</el-button>
                    <el-button type="primary" @click="ConfirmModifyCard"
                        :disabled="toModifyInfo.name.length === 0 || toModifyInfo.department.length === 0">确定</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- 删除借书证对话框 -->  
        <el-dialog v-model="removeCardVisible" title="删除借书证" width="30%">
            <span>确定删除<span style="font-weight: bold;">{{ toRemove }}号借书证</span>吗？</span>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="removeCardVisible = false">取消</el-button>
                    <el-button type="danger" @click="ConfirmRemoveCard">
                        删除
                    </el-button>
                </span>
            </template>
        </el-dialog>

    </el-scrollbar>
</template>

<script>
import {Delete, Edit, Files, Plus, Postcard, Search} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import borrow from "./Borrow.vue"


export default {

  computed: {
    Files() {
      return Files
    },
    Plus() {
      return Plus
    }
  },
  components: {Postcard},
    data() {
        return {
            cards: [{ // 借书证列表
                id: 1,
                name: '小明',
                department: '计算机学院',
                type: '学生'
            }, {
                id: 2,
                name: '王老师',
                department: '计算机学院',
                type: '教师'
            }
            ],
            Delete,
            Edit,
            Search,
            toSearch: '', // 搜索内容
            types: [ // 借书证类型
                {
                    value: '教师',
                    label: '教师',
                },
                {
                    value: '学生',
                    label: '学生',
                }
            ],
            newCardVisible: false, // 新建借书证对话框可见性
            removeCardVisible: false, // 删除借书证对话框可见性
            toRemove: 0, // 待删除借书证号
            newCardInfo: { // 待新建借书证信息
                name: '',
                department: '',
                type: '学生'
            },
            modifyCardVisible: false, // 修改信息对话框可见性
            toModifyInfo: { // 待修改借书证信息
                id: 0,
                name: '',
                department: '',
                type: '学生'  /* 你有病吧 */
            },
            newCardBoxVisible:true,
            onRedirect:false,
            redirectCard:0,
        }

    },
    methods: {
      ConfirmNewCard() {
        // 发出POST请求
        axios.post("/api/card?type=new",
            { // 请求体
              name: this.newCardInfo.name,
              department: this.newCardInfo.department,
              type: this.newCardInfo.type
            })
            .then(response => {
              if (response.data.ok) {
                ElMessage.success("新建ID为 " + response.data.payload + " 的借书证")
                this.newCardVisible = false;
                this.QueryCards()
                this.newCardBoxVisible = true;
              } else {
                if (response.data.message === "dupCard") {
                  ElMessage.error("该借书证已存在！")
                } else {
                  ElMessage.error("未知错误")
                }
              }
            })
      },
      ConfirmModifyCard() {
        axios.post("/api/card?type=modify",
            {
              name:this.toModifyInfo.name,
              department:this.toModifyInfo.department,
              type:this.toModifyInfo.type,
              card_id:this.toModifyInfo.id
            }).then(response=>{
          if (response.data.ok) {
            ElMessage.success("已成功修改信息")
            this.modifyCardVisible=false
            this.QueryCards()
          } else {
            if (response.data.message === "dupCard") {
              ElMessage.error("卡片重复")
            } else if(response.data.message === "cardNotFound"){
              ElMessage.error("该卡片不存在！")
            } else{
              ElMessage.error("未知错误")
            }
          }
        })
      },
      ConfirmRemoveCard() {
        axios.post("/api/card?type=remove",
            {
              card_id:this.toRemove,
            }).then(response=>{
              if (response.data.ok) {
                ElMessage.success("已删除该记录")
                this.removeCardVisible = false;
                this.QueryCards()
              } else {
                if(response.data.message==="cardNotFound"){
                  ElMessage.error("该卡片不存在！")
                }else if(response.data.message==="bookUnreturned"){
                  ElMessage.error("该卡片中有书尚未归还")
                }else{
                  ElMessage.error("未知错误")
                }
              }
        })
      },
      QueryCards() {
        this.cards = []
        let response = axios.get('/api/card?query=all')
            .then(response => {
              console.log(response)
              let cards = response.data.cards
              cards.forEach(card => {
                this.cards.push({
                  id: card.cardId,
                  department: card.department,
                  name: card.name,
                  type: card.type === 0 ? "学生" : "教师"
                })
              })
            })
        return this.cards
      },
      handleAddNew() {
        console.log("handleAddNew")
        this.newCardInfo.name = ''
        this.newCardInfo.department = ''
        this.newCardInfo.type = '学生'
        this.newCardVisible = true
        this.newCardBoxVisible = false
      },
      handleCancelAdd() {
        console.log("handleCancelAdd")
        this.newCardVisible = false
        this.newCardBoxVisible = true
      },
      handleEdit(card){
        this.toModifyInfo.id = card.id
        this.toModifyInfo.name = card.name
        this.toModifyInfo.department = card.department
        this.toModifyInfo.type = card.type
        this.modifyCardVisible = true
      },
      handleRedirect(card_id){
        this.redirectCard = card_id
        this.onRedirect = true
        this.$router.replace({ path: '/borrow/'+card_id })
      },
      fetchRD(){
        return {
          RD:this.onRedirect,
          card_id:this.redirectCard
        }
      }
    },
    mounted() { // 当页面被渲染时
        this.QueryCards() // 查询借书证
    },

}

</script>


<style scoped>
.cardBox {
    height: fit-content;
    width: 30%;
    border-radius: 20px;
    box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
    //text-align: center;
    margin-top: 40px;
    margin-left: 30px;
    margin-right: 10px;
}

.newCardBox {
    height: auto;
    width: 30%;
    max-width: 30%;
    border-radius: 20px;
    margin-top: 40px;
    margin-left: 30px;
    margin-right: 10px;
    padding: 7.5px;
    padding-right: 10px;
    box-shadow: 0 4px 8px 0 rgb(237, 237, 237), 0 6px 20px 0 rgba(195, 195, 195, 0.19);
    text-align: center;
}

.dag{
  border-radius: 20px;
}
</style>