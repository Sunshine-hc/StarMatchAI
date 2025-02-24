<template>
    <el-container class="app-container">
        <el-header>
            <el-menu mode="horizontal" router :default-active="$route.path">
                <el-menu-item index="/match">星座匹配</el-menu-item>
                <el-menu-item index="/match/history">历史记录</el-menu-item>
            </el-menu>
        </el-header>
        <el-main>
            <router-view />
        </el-main>
    </el-container>
</template>

<style scoped>
.app-container {
    min-height: 100vh;
}

.el-header {
    padding: 0;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.el-main {
    background-color: #f5f7fa;
}
</style>
