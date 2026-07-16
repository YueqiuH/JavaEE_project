<template>
    <div class="common-layout">
        <el-container>
            <el-header class="home-header">
                <el-menu :default-active="activeIndex" class="el-menu-demo" mode="horizontal" router
                    background-color="rgb(48,65,86)" text-color="rgb(191,203,217)" active-text-color="rgb(64,158,255)"
                    @select="handleSelect">
                    <el-menu-item index="1">
                        <div class="system-title">
                            智慧校园管理系统
                        </div>
                    </el-menu-item>
                    <div class="flex-grow"></div>
                    <div class="user-summary" v-if="currentUser">
                        <span>{{ currentUser.user.username }}</span>
                        <el-tag size="small" effect="plain">{{ roleLabel }}</el-tag>
                    </div>
                    <el-button class="logout-button" text :icon="SwitchButton" @click="logout">
                        退出登录
                    </el-button>
                </el-menu>
            </el-header>
            <el-main class="home-main">
                <router-view></router-view>
            </el-main>
        </el-container>
    </div>
</template>

<script setup>
    import { computed, onMounted, ref } from 'vue';
    import { useRouter } from 'vue-router';
    import { SwitchButton } from '@element-plus/icons-vue';
    import { getCurrentUser, logoutUser } from '@/api/auth.js';
    import { clearAccessToken } from '@/utils/authToken.js';
    import { clearStoredCurrentUser, getStoredCurrentUser, setStoredCurrentUser } from '@/utils/authSession.js';

    const router = useRouter();
    const activeIndex = ref('/main');
    const currentUser = ref(getStoredCurrentUser());
    const roleLabel = computed(() => currentUser.value?.roles?.[0] || '用户');

    const handleSelect = (key, keyPath) => {
        console.log(key, keyPath)
    }

    const loadCurrentUser = async () => {
        const result = await getCurrentUser();
        currentUser.value = result.data;
        setStoredCurrentUser(result.data);
    }

    const logout = async () => {
        try {
            await logoutUser();
        } finally {
            clearAccessToken();
            clearStoredCurrentUser();
            await router.push('/login');
        }
    }

    onMounted(() => {
        loadCurrentUser().catch(() => {
            // 请求拦截器负责提示并跳转登录页。
        });
    });
</script>

<style scoped>
    .home-header {
        padding: 0px;
    }
    .system-title {
        color: white;
        font-size: 20px;
        font-weight: bold;
        letter-spacing: 2px;
    }
    .flex-grow {
        flex-grow: 1;
    }
    .user-summary {
        align-items: center;
        color: rgb(191,203,217);
        display: flex;
        gap: 8px;
        margin-right: 8px;
    }
    .logout-button {
        color: rgb(191,203,217);
        margin-right: 16px;
    }
    .home-main {
        padding: 0px;
    }
</style>
