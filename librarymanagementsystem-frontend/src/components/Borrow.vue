<template>
    <el-scrollbar height="100%" style="width: 100%;">

        <!-- 标题和搜索框 -->
        <div style="margin-top: 20px; margin-left: 40px; font-size: 2em; font-weight: bold;">
            借书记录查询
            <el-input v-model="toSearch" :prefix-icon="Search" size="large"
                      style=" width: 15vw;min-width: 150px; margin-left: 30px; margin-right: 50px; float: right;margin-top:5px; "
                clearable />
        </div>

        <!-- 查询框 -->
      <div class="inner">
        <div style="width:30%; margin-bottom: 20px;">
            <el-input v-model="this.toQuery" style="display:inline; " placeholder="输入借书证ID"></el-input>
            <el-button style="margin-left: 10px;" plain color="#E54B3C" type="primary" @click="QueryBorrows" icon="Search">查询</el-button>
        </div>

        <el-text v-show="isShow" size="large" style="margin-top: 10px;">卡号为 {{toQuery}} 的用户有 {{tableData.length}} 条借书记录</el-text>
        <!-- 结果表格 -->
        <el-table :data="fitlerTableData" height="600px" empty-text="请进行查询"
            :default-sort="{ prop: 'borrowTime', order: 'ascending' }" :table-layout="'auto'"
            style="width: 100%; margin-left: 20px; margin-top: 30px; margin-right: 20px; max-width: 80vw;">
            <el-table-column prop="bookID" label="图书ID" sortable />
            <el-table-column prop="bookName" label="书名" sortable />
          <el-table-column prop="category" label="类别" sortable>
            <template #default="scope">
              <el-tag>{{ scope.row.bookCategory }}</el-tag>
            </template>
          </el-table-column>
            <el-table-column prop="borrowTime" label="借出时间" sortable />
            <el-table-column prop="returnTime" label="归还时间" sortable />
          <el-table-column label="操作">
            <template #default="scope">
              <el-button :disabled="scope.row.returnTime!=='-'"  size="small" @click="handleReturn(scope.row.bookID)">还书</el-button>

            </template>
          </el-table-column>

        </el-table>
      </div>
      <el-divider></el-divider>
    </el-scrollbar>
</template>

<script>
import axios from 'axios';
import { Search } from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from "element-plus";
import {property} from "lodash-es";
import card from "./Card.vue"
import {toRaw} from "vue";

export default {
    data() {
        return {
            isShow: false, // 结果表格展示状态
            tableData: [],
            toQuery: '', // 待查询内容(对某一借书证号进行查询)
            toSearch: '', // 待搜索内容(对查询到的结果进行搜索)
            Search
        }
    },
    computed: {
        fitlerTableData() { // 搜索规则
            return this.tableData.filter(
                (tuple) =>
                    (this.toSearch == '') || // 搜索框为空，即不搜索
                    tuple.bookID == this.toSearch || // 图书号与搜索要求一致
                    tuple.borrowTime.toString().includes(this.toSearch) || // 借出时间包含搜索要求
                    tuple.returnTime.toString().includes(this.toSearch) // 归还时间包含搜索要求
            )
        }
    },

    methods: {
      property,

        async QueryBorrows() {
            this.tableData = [] // 清空列表
            let response = await axios.get('/api/book', { params: { cardID: this.toQuery } }) // 向/borrow发出GET请求，参数为cardID=this.toQuery
            let borrows = response.data // 获取响应负载
          if(borrows.ok) {
            console.log(borrows.payload)
            borrows.payload.items.forEach(borrow => { // 对于每一个借书记录
              this.tableData.push({
                cardID: borrow.cardId,
                bookName: borrow.title,
                bookID: borrow.bookId,
                bookCategory:borrow.category,
                borrowTimestamp:borrow.borrowTime,
                borrowTime:  this.parseDate(borrow.borrowTime),
                returnTime: borrow.returnTime===0?"-" : this.parseDate(borrow.returnTime)
              }) // 将它加入到列表项中
            });
            ElMessage.success("成功查询")
          }else{
            if(borrows.message==="cardNotFound"){
              ElMessage.error("借阅证不存在")
            } else {
              ElMessage.error("未知错误")
            }
          }

            this.isShow = true // 显示结果列表
        },
      queryFromCard(card_id){
        this.toQuery=card_id;
        this.QueryBorrows();

      },
      parseDate(timestamp){
          var date = new Date(timestamp)
        var DD = String(date.getDate()).padStart(2, '0'); // 获取日
        var MM = String(date.getMonth() + 1).padStart(2, '0'); //获取月份，1 月为 0
        var yyyy = date.getFullYear(); // 获取年
        var hh =  String(date.getHours()).padStart(2, '0');       //获取当前小时数(0-23)
        var mm = String(date.getMinutes()).padStart(2, '0');     //获取当前分钟数(0-59)
        var ss = String(date.getSeconds()).padStart(2, '0');     //获取当前秒数(0-59)
        var todate = yyyy + '-' + MM + '-' + DD + ' ' + hh + ':' + mm;
        return todate
      },
      handleReturn(book_id){
        let toReturnBorrow = this.tableData.find(item=>item.bookID===book_id)
        console.log(toReturnBorrow)
        console.log(book_id)
        axios.post("/api/book?type=return",{
          book_id:book_id,
          card_id:this.toQuery,
          borrowTime:toReturnBorrow.borrowTimestamp
        }).then(response => {
          if(response.data.ok){
            ElMessage.success("还书成功")
            this.QueryBorrows()
          } else if(response.data.message==="notBorrowed") {
            ElMessage.error("未借阅此书")
          } else {
            ElMessage.error("未知错误")
          }
        })
      }
    },
  mounted() {
    console.log(this.$route.params.id)
    if(this.$route.params.id!=0)
    {
      this.toQuery = this.$route.params.id
      this.QueryBorrows()
    }

  },

}
</script>

<style scoped>
.inner{
  margin-left: 40px;
  margin-top: 20px;
}
</style>