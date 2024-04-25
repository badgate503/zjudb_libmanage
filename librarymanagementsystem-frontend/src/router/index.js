import { createRouter, createWebHistory } from 'vue-router'
import BookVue from '@/components/Book.vue'
import CardVue from '@/components/Card.vue'
import BorrowVue from '@/components/Borrow.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/book'
    },
    {
      path: '/book',
      component: BookVue
    },
    {
      path: '/card',
      component: CardVue
    },
    {
      path: '/borrow/:id',
      component: BorrowVue
    },
    {
      path: '/borrow/',
      redirect: '/borrow/0'
    },
    {
      path: '/borrow/borrow',
      redirect: '/borrow/0'
    },
    {
      path: '/borrow/book',
      redirect: '/book'
    },
    {
      path: '/borrow/card',
      redirect: '/card'
    },
  ]
})

export default router
