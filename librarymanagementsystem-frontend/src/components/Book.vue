<!-- TODO: YOUR CODE HERE -->
<template>
    <el-scrollbar style="width: 100%; height: 100%; ">
        <div style="margin-top: 20px; margin-left: 40px; font-size: 2em; font-weight: bold; ">图书管理</div>

        <div class="queries">
          <el-drawer v-model="drawer" title="查询书籍" size="40%">
            <div class="drawerInner">
              <el-form :model="form" label-width="auto" style="max-width: 600px">
                <el-form-item label="图书分类">
                  <el-select v-model="category" placeholder="请选择一个分类">
                    <el-option
                      v-for="item in cateList"
                      :key="item.key"
                      :label="item.label"
                      :value="item.key"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="书名">
                  <el-input v-model="title" />
                </el-form-item>
                <el-form-item label="作者">
                  <el-col :span="10">
                    <el-input
                        v-model="author"
                        placeholder="作者姓名"
                        style="width: 100%"
                    />
                  </el-col>

                  <el-col :span="3" class="text-center">
                    <span class="text-gray-500">出版社</span>
                  </el-col>
                  <el-col :span="11">
                    <el-input
                        v-model="press"
                        placeholder="出版社名"
                        style="width: 100%"
                    />
                  </el-col>
                </el-form-item>

                <el-form-item label="出版年份">
                  <el-slider v-model="yearRange" range :min="minYear" :max = "maxYear"/>
                </el-form-item>

                <el-form-item label="价格区间">
                  <el-col :span="3">
                    <el-switch v-model="noQueryPrice" @change="doQueryPrice = !noQueryPrice" />
                  </el-col>
                  <el-col :span="10">
                    <el-input
                        placeholder="最低价格"
                        v-model="minPrice"
                        v-model:disabled="doQueryPrice"
                        :formatter="(minPrice) => `${minPrice}`.replace(/[a-zA-Z]*/g, '')"
                        style="width: 100%"
                    >
                      <template #prepend>¥</template>
                    </el-input>
                  </el-col>
                  <el-col :span="2" class="text-center">
                    <span class="text-gray-500">-</span>
                  </el-col>
                  <el-col :span="9">
                    <el-input
                        placeholder="最高价格"
                        v-model="maxPrice"
                        v-model:disabled="doQueryPrice"
                        :formatter="(maxPrice) => `${maxPrice}`.replace(/[a-zA-Z]*/g, '')"
                        style="width: 100%"
                    >
                      <template #prepend>¥</template>
                    </el-input>
                  </el-col>
                </el-form-item>

                <el-form-item>
                  <el-col :span="11">

                  </el-col>

                  <el-col :span="3">
                    <el-button  color="#E54B3C" @click="QueryBooks" style="width: 100px" icon="Search" >查询</el-button>
                  </el-col>
                  <el-col :span="3">

                  </el-col>
                  <el-col :span="3">
                    <el-button  @click="ClearQuery" color="#E54B3C" plain>清除</el-button>
                  </el-col>
                  <el-col :span="1">

                  </el-col>
                  <el-col :span="3">
                    <el-button @click="closeRightDrawer" color="#E54B3C" plain>取消</el-button>
                  </el-col>
                </el-form-item>
              </el-form>
            </div>

          </el-drawer>

          <el-drawer v-model="addBookDrawer" title="添加书籍记录"  size="40%">
            <div class="drawerInner">
              <el-form  label-width="auto" style="max-width: 600px">
                <el-form-item label="图书分类" required>
                  <el-select v-model="newBookCategory"
                             allow-create
                             filterable
                             placeholder="选择或创建一个分类">
                    <el-option
                        v-for="item in cateList"
                        :key="item.key"
                        :label="item.key"
                        :value="item.key"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="书名" required>
                  <el-input v-model="newBookTitle" />
                </el-form-item>
                <el-form-item label="作者" required>
                  <el-col :span="10">
                    <el-input
                        v-model="newBookAuthor"
                        placeholder="作者姓名"
                        style="width: 100%"
                    />
                  </el-col>
                  <el-col :span="4" class="text-center">
                    <span class="text-gray-500">出版社</span>
                  </el-col>
                  <el-col :span="10">
                    <el-input
                        v-model="newBookPress"
                        placeholder="出版社名"
                        style="width: 100%"
                    />
                  </el-col>
                </el-form-item>
                <el-form-item label="出版年份" required>
                  <el-col :span="12">
                    <el-date-picker
                        v-model="newBookPublishYear"
                        type="year"
                        format="YYYY"
                        value-format="YYYY"
                        placeholder="选择年份"
                    />
                  </el-col>
                  <el-col :span="4" class="text-center">
                    <span class="text-gray-500">价格</span>
                  </el-col>
                  <el-col :span="8">
                    <el-input
                        placeholder="价格"
                        @change="validateNewBook"
                        v-model="newBookPrice"
                        :formatter="(minPrice) => `${minPrice}`.replace(/[a-zA-Z]*/g, '')"
                        style="width: 100%"
                    >
                      <template #prepend>¥</template>
                    </el-input>
                  </el-col>
                </el-form-item>
                <el-form-item label="数量">
                  <el-col :span="4">
                    <el-input-number @change="validateNewBook" v-model="newBookStock"/>
                  </el-col>
                  <el-col :span="7">
                  </el-col>
                  <el-col :span="3">
                    <el-button  color="#E54B3C" @click="addBook"  icon="Plus" >添加</el-button>
                  </el-col>
                  <el-col :span="2">
                  </el-col>
                  <el-col :span="3">
                    <el-button  @click="ClearNewBook" color="#E54B3C" plain>清除</el-button>
                  </el-col>
                  <el-col :span="1">
                  </el-col>
                  <el-col :span="3">
                    <el-button @click="addBookDrawer=false" color="#E54B3C" plain>取消</el-button>
                  </el-col>
                </el-form-item>
              </el-form>
            </div>

          </el-drawer>

          <el-drawer v-model="editBookDrawer" title="编辑书籍记录"  size="40%">
            <div class="drawerInner">
              <el-form  label-width="auto" style="max-width: 600px">
                <el-form-item label="图书分类" required>
                  <el-select v-model="editBookCategory"
                             allow-create
                             filterable
                             placeholder="选择或创建一个分类">
                    <el-option
                        v-for="item in cateList"
                        :key="item.key"
                        :label="item.key"
                        :value="item.key"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="书名" required>
                  <el-input v-model="editBookTitle" />
                </el-form-item>
                <el-form-item label="作者" required>
                  <el-col :span="10">
                    <el-input
                        v-model="editBookAuthor"
                        placeholder="作者姓名"
                        style="width: 100%"
                    />
                  </el-col>
                  <el-col :span="4" class="text-center">
                    <span class="text-gray-500">出版社</span>
                  </el-col>
                  <el-col :span="10">
                    <el-input
                        v-model="editBookPress"
                        placeholder="出版社名"
                        style="width: 100%"
                    />
                  </el-col>
                </el-form-item>
                <el-form-item label="出版年份" required>
                  <el-col :span="12">
                    <el-date-picker
                        v-model="editBookPublishYear"
                        type="year"
                        format="YYYY"
                        value-format="YYYY"
                        placeholder="选择年份"
                    />
                  </el-col>
                  <el-col :span="4" class="text-center">
                    <span class="text-gray-500">价格</span>
                  </el-col>
                  <el-col :span="8">
                    <el-input
                        placeholder="价格"
                        @change="validateEditBook"
                        v-model="editBookPrice"
                        :formatter="(minPrice) => `${minPrice}`.replace(/[a-zA-Z]*/g, '')"
                        style="width: 100%"
                    >
                      <template #prepend>¥</template>
                    </el-input>
                  </el-col>
                </el-form-item>
                <el-form-item>

                  <el-col :span="12">
                  </el-col>
                  <el-col :span="3">
                    <el-button  color="#E54B3C" @click="confirmEditBook"  icon="Edit" >修改</el-button>
                  </el-col>
                  <el-col :span="2">
                  </el-col>
                  <el-col :span="3">
                    <el-button  @click="initEdit" color="#E54B3C" plain>复原</el-button>
                  </el-col>
                  <el-col :span="1">
                  </el-col>
                  <el-col :span="3">
                    <el-button @click="editBookDrawer=false" color="#E54B3C" plain>取消</el-button>
                  </el-col>
                </el-form-item>
              </el-form>
            </div>

          </el-drawer>

          <el-drawer v-model="borrowBookDrawer" title="借书"  size="40%">
            <div class="drawerInner">
              <el-descriptions
                  class="margin-top"
                  title="书籍信息"
                  :column="2"
                  :size="size"
                  border
              >
                <el-descriptions-item>
                  <template #label>
                    <div class="cell-item">
                      <el-icon :style="iconStyle">
                        <notebook />
                      </el-icon>
                      书名
                    </div>
                  </template>
                  {{borrowedBook.title}}
                </el-descriptions-item>
                <el-descriptions-item>
                  <template #label>
                    <div class="cell-item">
                      <el-icon :style="iconStyle">
                        <office-building />
                      </el-icon>
                      出版社
                    </div>
                  </template>
                  {{borrowedBook.press}}
                </el-descriptions-item>
                <el-descriptions-item>
                  <template #label>
                    <div class="cell-item">
                      <el-icon :style="iconStyle">
                        <user />
                      </el-icon>
                      作者
                    </div>
                  </template>
                  {{ borrowedBook.author }}
                </el-descriptions-item>
                <el-descriptions-item>
                  <template #label>
                    <div class="cell-item">
                      <el-icon :style="iconStyle">
                        <tickets />
                      </el-icon>
                      分类
                    </div>
                  </template>
                  <el-tag size="small">{{borrowedBook.category}}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item>
                  <template #label>
                    <div class="cell-item">
                      <el-icon :style="iconStyle">
                        <timer />
                      </el-icon>
                      出版年份
                    </div>
                  </template>
                  {{borrowedBook.publishYear }}
                </el-descriptions-item>
                <el-descriptions-item>
                  <template #label>
                    <div class="cell-item">
                      <el-icon :style="iconStyle">
                        <van />
                      </el-icon>
                      库存
                    </div>
                  </template>
                  {{borrowedBook.stock }}
                </el-descriptions-item>
              </el-descriptions>


              <el-card class="cardBox" style="max-width: 600px" >
                <template #header>
                  <div class="card-header" style="display: flex;flex-direction: column;justify-content: center;align-items: center;">
                    <div>
                      <el-icon style="height: 25px; width: 25px;" color="#00CC00" v-show="borrowerCard.isOk">
                        <Check style="height: 100%; width: 100%;" />
                      </el-icon>
                      <el-icon style="height: 25px; width: 25px;" color="#CC0000" v-show="!borrowerCard.isOk">
                        <Close style="height: 100%; width: 100%;" />
                      </el-icon>
                    </div>
                    <div>
                      借阅凭证 #<el-text size="large" type="primary" tag="b">{{borrowerCard.id}}</el-text>
                    </div>

                  </div>

                </template>
                <div style="width: 100%" >
                  <el-row >姓名：{{borrowerCard.name}}</el-row>
                  <el-row style="margin-top: 10px;">部门：{{borrowerCard.department}}</el-row>
                  <el-row style="margin-top: 10px;">类型：<el-tag>{{borrowerCard.type}}</el-tag></el-row>
                </div>
                <template #footer>
                  <div style="width:100%;" >
                    <el-row ><el-input v-model="borrowerCard.id" @change="onSearchCard"  placeholder="请输入借阅证编号"></el-input></el-row>
                  </div>


                </template>




              </el-card>


              <el-form  label-width="auto" style="max-width: 600px; margin-top: 30px;" >
                <el-form-item>

                  <el-col :span="14">
                  </el-col>
                  <el-col :span="3">
                    <el-button :disabled="!borrowerCard.isOk" color="#E54B3C" @click="confirmBorrowBook"  icon="Check" >确认借阅</el-button>
                  </el-col>
                  <el-col :span="2">
                  </el-col>

                  <el-col :span="1">
                  </el-col>
                  <el-col :span="3">
                    <el-button @click="borrowBookDrawer=false" color="#E54B3C" plain>取消</el-button>
                  </el-col>
                </el-form-item>
              </el-form>
            </div>

          </el-drawer>

          <el-form :model="form" label-width="auto" style="max-width: 600px">
            <el-form-item >
              <el-col :span="5">
                <el-button color="#E54B3C" type="primary" @click="drawer = true" plain icon="Filter">
                  设置筛选
                </el-button>
              </el-col>

              <el-col :span="5">
                <el-button color="#E54B3C" type="primary" @click="addBookDrawer=true" plain icon="Plus">
                  添加书籍
                </el-button>
              </el-col>
              <el-col :span="3">
                <el-popover
                    placement="right-end"
                    title="选择文件"
                    :width="300"
                    trigger="click"
                    :visible="showOpenfile"
                    @close="clearFile"
                >
                  <template #reference>
                    <el-button color="#E54B3C" type="primary" @click="handleShowPop" plain icon="Upload">
                      批量导入
                    </el-button>
                  </template>
                  <el-upload
                      ref="upload"
                      class="upload-demo"
                      action="#"
                      :limit="1"
                      :on-exceed="handleFileExceed"
                      :before-upload="beforeFileUpload"
                      :auto-upload="false"
                      :file-list="upload.fileList"
                      :on-change="handleFileChange"
                      @close="clearFile"
                  >

                    <template #trigger>
                      <el-button type="primary" icon="Document" color="#E54B3C">选择文件</el-button>
                    </template>

                    <template #tip>
                      <div class="el-upload__tip text-red">
                        请上传.json 文件 (最多一份)
                      </div>
                    </template>


                  </el-upload>
                  <el-button class="ml-3" style=""  @click="submitUpload">
                    确认
                  </el-button>
                  <el-button class="ml-3" style=""  @click="onPopClose">
                    取消
                  </el-button>
                  <el-button class="ml-3" style="margin-left:10px"  color="#E54B3C" @click="clearFile">
                    清除
                  </el-button>
                </el-popover>

              </el-col>
            </el-form-item>

            <el-form-item label="排序方式" >
              <el-col :span="10">
                <el-form-item >
                  <el-select @change="reloadOrder" placeholder="排序方式" v-model="sortByValue">
                    <el-option label="书籍编号" value="BOOK_ID" />
                    <el-option label="书名" value="TITLE" />
                    <el-option label="作者姓名" value="AUTHOR" />
                    <el-option label="出版社" value="PRESS" />
                    <el-option label="类别" value="CATEGORY" />
                    <el-option label="出版年份" value="PUBLISH_YEAR" />
                    <el-option label="价格" value="PRICE" />
                    <el-option label="存量" value="STOCK" />

                  </el-select>
                </el-form-item>
              </el-col>

              <el-col :span="4" class="text-center" >
                <span class="text-gray-500">顺序</span>
              </el-col>
              <el-col :span="9">
                <el-radio-group color="#E54B3C" v-model="ascOrDesc" @change="reloadOrder" class="ml-4">
                  <el-radio value="ASC"  border>升序</el-radio>
                  <el-radio value="DESC"  border>降序</el-radio>
                </el-radio-group>
              </el-col>

            </el-form-item>

          </el-form>

          <el-table-v2
              :columns="bookSchema"
              :data="bookDataSet"
              :width="1200"
              :height="900"
          />

          <el-dialog
              v-model="addStockDialog"
              :title="'修改 '+addStockBookName+' 的库存'"
              width="600"
          >
            调整或输入新库存：<el-input-number min="0" v-model="addStockBookStock"></el-input-number>
            <template #footer>
              <div class="dialog-footer">
                <el-button @click="addStockDialog = false">取消</el-button>
                <el-button color="#E54B3C" @click="confirmEditStock" icon="Check">
                  确定
                </el-button>
              </div>
            </template>
          </el-dialog>

          <el-divider/>


        </div>

    </el-scrollbar>
</template>

<script lang="jsx">
import "@/assets/Book.css"
import {ref, toRaw} from 'vue'
import {autoResizerProps, ElMessage, TableV2SortOrder, ElButton, ElMessageBox} from "element-plus";
import axios from "axios"
import {Check, Filter, Notebook, OfficeBuilding, Present} from "@element-plus/icons-vue";
import cards from "./Card.vue"

export default {
  components: {Check, OfficeBuilding, Present, Notebook, Filter},
  data(){
    return{
      doQueryPrice: true,
      noQueryPrice: false,
      minYear : 1945,
      maxYear : 2024,
      yearRange : ref([1945,2024]),
      minPrice : 0,
      maxPrice :0,
      sortByValue : "BOOK_ID",
      ascOrDesc : "ASC",
      drawer: false,
      addBookDrawer: false,
      editBookDrawer: false,
      borrowBookDrawer: false,
      borrowedBook:{},
      queryPayLoad: {},
      bookSchema: [
        {
          key: "bookId",
          title: "编号",
          dataKey: "bookId",
          width: "60"
        },
        {
          key: "title",
          dataKey: "title",
          title: "书名",
          width:"250"
        },
        {
          key: "author",
          dataKey: "author",
          title: "作者",
          width: "110"
        },{
          key: "press",
          dataKey: "press",
          title: "出版社",
          width: "180"
        },{
          key: "category",
          dataKey: "category",
          title: "类别",
          width:"140",
         cellRenderer: ({ cellData: name }) => <ElTag>{name}</ElTag>,
        },{
          key: "publishYear",
          dataKey: "publishYear",
          title: "出版年份",
          width: "100"
        },{
          key: "price",
          dataKey: "price",
          title: "价格",
          width: "90"
        },{
          key: "stock",
          dataKey: "stock",
          title: "存量",
          width: "70",
        },{
          key: "",
          dataKey: "bookId",
          title: "操作",
          width: "260",
          cellRenderer: (bookId)=>(
              <>
                <ElButton size="small" onClick={()=>this.borrowBook(bookId.cellData)}>借阅</ElButton>
                <ElButton size="small" onClick={()=>this.editBook(bookId.cellData)}>编辑</ElButton>
                <ElButton size="small" onClick={()=>this.editStock(bookId.cellData)}>库存</ElButton>
                <ElButton size="small" color="#E54B3C" onClick={()=>this.deleteBook(bookId.cellData)}>删除</ElButton>
              </>
          )
        },
      ],
      bookDataSet:[
        {
          bookId:186,
          title:"Computer Organization and Design (RISC-V)",
          author:"A. Patterson",
          press:"Pearson",
          category:"Computer Science",
          publishYear:2022,
          price:"¥108.5",
          stock:46
        }
      ],
      newBookCategory:"",
      newBookTitle:"",
      newBookAuthor:"",
      newBookPress:"",
      newBookPublishYear:ref('2024'),
      newBookPrice:0.0,
      newBookStock:0,
      editBookCategory:"",
      editBookTitle:"",
      editBookAuthor:"",
      editBookPress:"",
      editBookPublishYear:ref(''),
      editBookPrice:0,
      addStockDialog:false,
      addStockBookID:0,
      addStockBookName:"",
      addStockBookStockOrigin:0,
      addStockBookStock:0,
      category:"",
      cateList:[{label:"所有",key:""}],
      author:"",
      press:"",
      showOpenfile:false,
      title:"",
      editingBookID:0,
      upload: {
        isUploading: false,
        headers: {Authorization: "Bearer "},
        fileList: []
      },
      cards:[],
      borrowerCard:{
        name:"未找到",
        department:"未找到",
        type:"未找到",
        id:null,
        isOk:false,
      },
    }
  },
  methods:{
    QueryBooks() {

      var that = this;

      if(this.noQueryPrice && ((!this.minPrice.toString().match("^(([1-9]{1}\\d*)|(0{1}))(\\.\\d{1,2})?$"))||(!this.maxPrice.toString().match("^(([1-9]{1}\\d*)|(0{1}))(\\.\\d{1,2})?$")))) {
        ElMessage.error("价格格式错误");

      } else if(this.noQueryPrice && this.maxPrice===0){
        ElMessage.error("最大价格不能为0");

      } else{
        this.bookDataSet = []
        axios.post("/api/book?type=query",{
          queryPrice:this.noQueryPrice,
          category:this.category,
          title:this.title,
          press:this.press,
          minPublishYear:toRaw(this.yearRange)[0],
          maxPublishYear:toRaw(this.yearRange)[1],
          author:this.author,
          minPrice:parseFloat(this.minPrice),
          maxPrice:parseFloat(this.maxPrice),
          sortBy:this.sortByValue,
          sortOrder:this.ascOrDesc
        }).then(response => {
          console.log(response)
          let resset = response.data
          this.drawer = false;   // The query drawer shall be closed.
          resset.forEach(book => {
            that.bookDataSet.push(book)
          });
        }).catch(e=>{
          console.log(e)
        })
      }



    },
    addBook(){
      if(this.newBookCategory==="")
      {ElMessage.error("请选择或输入分类");return;}
      if(this.newBookTitle==="")
      {ElMessage.error("请输入书名");return;}
      if(this.newBookAuthor==="")
      {ElMessage.error("请输入作者");return;}
      if(this.newBookPress==="")
      {ElMessage.error("请输入出版社");return;}
      if((!this.newBookPrice.toString().match("^(([1-9]{1}\\d*)|(0{1}))(\\.\\d{1,2})?$"))||this.newBookPrice.toString()==="0")
      {ElMessage.error("价格格式错误或为0");return;}
      if(this.newBookStock===0)
      {ElMessage.error("数量不应为 0");return;}
      console.log(toRaw(this.newBookPublishYear));
      axios.post("/api/book?type=add",{
        category:this.newBookCategory,
        title:this.newBookTitle,
        press:this.newBookPress,
        publishYear:toRaw(this.newBookPublishYear),
        author:this.newBookAuthor,
        price:parseFloat(this.newBookPrice),
        stock:parseInt(this.newBookStock)
      }).then(response => {
        if(response.data.ok)
        {
          ElMessage.success("成功添加图书")
          if(parseInt(toRaw(this.newBookPublishYear))>this.maxYear)
            this.maxYear=parseInt(toRaw(this.newBookPublishYear))
          if(parseInt(toRaw(this.newBookPublishYear))<this.minYear)
            this.minYear=parseInt(toRaw(this.newBookPublishYear))
          this.yearRange=ref([this.minYear,this.maxYear])
          this.queryCategory()
          this.QueryBooks()
          this.addBookDrawer = false;
          this.ClearNewBook()
        }
        else
        {
          if(response.data.message === "dupBook")
            ElMessage.error("添加失败：该记录已存在")
          else
            ElMessage.error("添加失败：未知错误")
        }
      }).catch(e=>{
        console.log(e)
      })

    },
    editBook(bookId){
      this.editingBookId = bookId;
      this.initEdit()
      this.editBookDrawer = true;
    },
    editStock(bookId){
      this.addStockBookID = bookId;
      let bookInfo = this.bookDataSet.find(item=>item.bookId===bookId)
      this.addStockBookName = bookInfo.title;
      this.addStockBookStock = bookInfo.stock;
      this.addStockBookStockOrigin = bookInfo.stock;
      this.addStockDialog = true;
    },
    confirmEditStock(){
      if(this.addStockBookStock<0)
      { ElMessage.error("库存不能小于 0"); return; }
      axios.post("/api/book?type=stock",{
        book_id:this.addStockBookID,
        deltaStock:this.addStockBookStock - this.addStockBookStockOrigin,
      }).then(response => {
        if(response.data.ok)
        {
          ElMessage.success("成功更新图书库存")

          this.QueryBooks()
          this.addStockDialog = false;
        }
        else
        {
          if(response.data.message === "bookNotFound")
            ElMessage.error("调整库存失败：该图书不存在")
          else
            ElMessage.error("调整库存失败：未知错误")
        }
      }).catch(e=>{
        console.log(e)
      })
    },
    initEdit(){
      let bookInfo = this.bookDataSet.find(item=>item.bookId===this.editingBookId)
      this.editBookAuthor = bookInfo.author;
      this.editBookPress = bookInfo.press;
      this.editBookPrice = bookInfo.price;
      this.editBookPublishYear = new ref(bookInfo.publishYear.toString());
      this.editBookTitle = bookInfo.title;
      this.editBookCategory = bookInfo.category;
    },
    confirmEditBook(){
      if(this.editBookCategory==="")
      {ElMessage.error("请选择或输入分类");return;}
      if(this.editBookTitle==="")
      {ElMessage.error("请输入书名");return;}
      if(this.editBookAuthor==="")
      {ElMessage.error("请输入作者");return;}
      if(this.editBookPress==="")
      {ElMessage.error("请输入出版社");return;}
      if((!this.editBookPrice.toString().match("^(([1-9]{1}\\d*)|(0{1}))(\\.\\d{1,2})?$"))||this.editBookPrice.toString()==="0")
      {ElMessage.error("价格格式错误或为0");return;}

      axios.post("/api/book?type=edit",{
        book_id:this.editingBookId,
        category:this.editBookCategory,
        title:this.editBookTitle,
        press:this.editBookPress,
        publishYear:toRaw(this.editBookPublishYear),
        author:this.editBookAuthor,
        price:parseFloat(this.editBookPrice),
      }).then(response => {
        if(response.data.ok)
        {
          ElMessage.success("成功编辑图书信息")
          if(parseInt(toRaw(this.editBookPublishYear))>this.maxYear)
            this.maxYear=parseInt(toRaw(this.editBookPublishYear))
          if(parseInt(toRaw(this.editBookPublishYear))<this.minYear)
            this.minYear=parseInt(toRaw(this.editBookPublishYear))
          this.yearRange=ref([this.minYear,this.maxYear])
          this.queryCategory()
          this.QueryBooks()
          this.editBookDrawer = false;
          this.ClearNewBook()
        }
        else
        {
          if(response.data.message === "bookNotFound")
            ElMessage.error("添加失败：该图书不存在")
          else
            ElMessage.error("添加失败：未知错误")
        }
      }).catch(e=>{
        console.log(e)
      })
    },
    deleteBook(bookId){
      console.log(bookId)
      ElMessageBox.confirm(
          '该记录将会被永久删除且不可恢复，是否继续？',
          '警告',
          {
            confirmButtonText:'确定',
            cancelButtonText:'取消',
            type:'warning'
          }
      ).then(()=>{
        axios.post("/api/book?type=delete",{
          book_id:bookId,
        }).then(response => {
          if(response.data.ok)
          {
            ElMessage.success("成功删除图书")
            this.queryMinMaxYear()
            this.queryCategory()
            this.QueryBooks()
          }
          else
          {
            if(response.data.message === "bookNoFound")
              ElMessage.error("删除失败：书籍记录不存在")
            else if(response.data.message === "bookNotReturn")
              ElMessage.error("删除失败：至少有一本书未被归还")
            else
              ElMessage.error("添加失败：未知错误")
          }
        }).catch(e=>{
          console.log(e)
        })
      })
    },
    closeRightDrawer(){
      this.drawer = false;
    },
    async queryCategory(){
      var that = this
      this.cateList = [{label:"所有",key:""}]
      axios.get('/api/book',{params:{queryType:"category"}}).then(function (response) {
        let resset = response.data
        resset.forEach(cat => {
          that.cateList.push({
            label:cat,
            key:cat
          })
        });
      })
    },
    async queryMinMaxYear(){
      var that = this
      axios.get('/api/book',{params:{queryType:"year"}}).then(function (response) {
        let resset = response.data
        console.log(resset);
        that.minYear = parseInt(resset[0])
        that.maxYear = parseInt(resset[1])
        that.yearRange = ref([that.minYear,that.maxYear])
      })
    },
    clearFile(){
      this.upload.fileList=[];
    },
    handleFileChange(file, fileList){

      console.log(file)
      const isJson = file.raw.type === 'application/json'
      if(!isJson) {
        ElMessage.error("文件类型只能为.json !")
        this.clearFile()
      }
      else{
        this.upload.fileList = fileList
      }
      return isJson
    },
    handleFileExceed(fileList){
      this.upload.fileList = fileList
    },
    async submitUpload(){
      if(this.upload.fileList.length===0){
        ElMessage.error("请选择文件")
        return;
      }
      const formData = new FormData();
      console.log(this.upload.fileList[0])
      formData.append('file', this.upload.fileList[0].raw); // fileInput 为 <input type="file" />
      axios.post('/api/book?queryType=file', formData, {headers: {
          'Content-Type': 'multipart/form-data'
      }}).then(async (response) => {
        if(response.data.ok){
          this.onPopClose();
          ElMessage.success("成功添加 "+response.data.payload+" 本书")
          this.upload.fileList=[]
          this.yearRange=ref([0,9999])
          await this.queryMinMaxYear()
          await this.queryCategory()
          this.QueryBooks()
        }
        else {
          console.log(response)
          if (response.data.message === "wrongFormat") {
            ElMessage.error(".json 文件结构错误")
          } else if (response.data.message === "dupBook") {
            ElMessage.error("至少有一本书与现有记录重复")
          } else {
            ElMessage.error("未知错误")
          }
        }
      })
    },
    onPopClose(){
      this.showOpenfile=false;
      this.clearFile();
    },
    handleShowPop(){
      if(!this.showOpenfile)
        this.showOpenfile = true;
      else
        this.onPopClose();
    },
    reloadOrder(){
      this.QueryBooks()
    },
    ClearQuery(){
      this.category=""
      this.maxPrice=0
      this.maxPrice=0
      this.author=""
      this.press=""
      this.title = ""
      this.yearRange=ref([this.minYear,this.maxYear])
    },
    ClearNewBook() {
      this.newBookPublishYear = ref('2024')
      this.newBookTitle = "";
      this.newBookAuthor = "";
      this.newBookCategory = "";
      this.newBookPress = "";
      this.newBookPrice = 0;
      this.newBookStock = 0;
    },
    validateNewBook(){
      if(this.newBookPrice<0)
        this.newBookPrice=0;
      if(this.newBookStock<0)
        this.newBookStock=0;
    },
    validateEditBook(){
      if(this.editBookPrice<0)
        this.editBookPrice=0;
    },
    onSearchCard(){
      console.log(this.borrowerCard.id)
      console.log(this.cards)
      let cardInfo = this.cards.find(item=>item.id===parseInt(this.borrowerCard.id))
      console.log(cardInfo)
      if(cardInfo===undefined){
        this.borrowerCard.name="未找到"
        this.borrowerCard.department="未找到"
        this.borrowerCard.type="未找到"
        this.borrowerCard.isOk=false
      }
      else{
        this.borrowerCard.name=cardInfo.name
        this.borrowerCard.department=cardInfo.department
        this.borrowerCard.type=cardInfo.type
        this.borrowerCard.isOk=true
      }
    },
    borrowBook(bookId){
      this.borrowBookDrawer = true;
      this.borrowedBook = this.bookDataSet.find(item=>item.bookId===bookId)
    },
    confirmBorrowBook(){
      axios.post("/api/book?type=borrow",{
        book_id:this.borrowedBook.bookId,
        card_id:this.borrowerCard.id
      }).then(response => {
        if(response.data.ok){
          ElMessage.success("借阅成功！")
          this.QueryBooks()
          this.borrowBookDrawer = false;
          this.borrowerCard.id=0;
          this.borrowerCard.name="未找到"
          this.borrowerCard.department="未找到"
          this.borrowerCard.type="未找到"
          this.borrowerCard.isOk=false
        }else if(response.data.message==="noStock"){
          ElMessage.error("该图书暂无库存")
        }else if(response.data.message==="bookUnreturned"){
          ElMessage.error("该卡已借阅此书尚未归还")
        }else{
          ElMessage.error("未知错误")
        }
      }



      )

    }
  },
  created() {
    this.cards=toRaw(cards.methods.QueryCards())
    console.log(cards)

    this.QueryBooks();
    this.queryCategory();
    this.queryMinMaxYear();
  },



}

</script>

<style scoped>
.cardBox {
  height: fit-content;
  width: 100%;
  border-radius: 20px;
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
  //text-align: center;
  margin-top: 40px;
  margin-right: 10px;
}
</style>