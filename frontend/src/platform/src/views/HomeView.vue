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
                </el-menu>
            </el-header>
            <el-main class="home-main">
                <router-view></router-view>
            </el-main>
        </el-container>
    </div>
</template>

<script setup>
    import { ref } from 'vue';

    const activeIndex = ref('/main');

    const handleSelect = (key, keyPath) => {
        console.log(key, keyPath)
    }
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
    .home-main {
        padding: 0px;
    }
</style>
