import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import BookDetailView from '../views/BookDetailView.vue';

const routes = [
    {
        path: '/',
        name: 'home',
        component: HomeView,
    },
    {
        path: '/book/:isbn', // :isbn은 동적으로 변하는 파라미터
        name: 'bookDetail',
        component: BookDetailView,
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;