<template>
  <div class="d-page">
    <div class="d-crumb d-rise" style="--rise: 0">
      <router-link to="/home">首页</router-link>
      <el-icon><ArrowRight /></el-icon>
      <router-link :to="{ path: '/services', query: { domain: 'base' } }">基础数据</router-link>
      <el-icon><ArrowRight /></el-icon>
      <span>新闻与论坛</span>
    </div>

    <header class="d-head d-rise" style="--rise: 1">
      <div>
        <span class="d-head-module">基础数据 · D5</span>
        <h1>新闻公告与校园论坛</h1>
        <p class="d-head-desc">浏览学校新闻公告，参与校园话题交流</p>
      </div>
    </header>

    <section class="d-panel tab-panel d-rise" style="--rise: 2">
      <el-tabs v-model="activeTab">
        <!-- ===== 新闻公告 ===== -->
        <el-tab-pane label="新闻公告" name="news">
          <div class="filter-bar">
            <el-input
              v-model="newsQuery.keyword" clearable :prefix-icon="Search" placeholder="标题 / 内容关键字"
              class="filter-keyword" @keyup.enter="loadNews(1)" @clear="loadNews(1)"
            />
            <el-select v-model="newsQuery.newsType" clearable placeholder="全部类型" class="filter-select" @change="loadNews(1)">
              <el-option label="公告" value="公告" />
              <el-option label="新闻" value="新闻" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="loadNews(1)">查询</el-button>
            <span class="filter-spacer"></span>
            <el-button v-if="canWrite" type="primary" :icon="Plus" @click="openNewsForm()">发布</el-button>
          </div>

          <div v-loading="newsLoading" class="news-list">
            <el-empty v-if="!newsLoading && newsRows.length === 0" description="暂无内容" />
            <article v-for="news in newsRows" :key="news.newsId" class="news-item" @click="openNewsDetail(news)">
              <div class="news-title-line">
                <el-tag v-if="news.isPinned === 1" type="danger" size="small" effect="dark">置顶</el-tag>
                <el-tag size="small" effect="plain" :type="news.newsType === '公告' ? 'warning' : 'info'">{{ news.newsType }}</el-tag>
                <strong>{{ news.title }}</strong>
              </div>
              <p class="news-summary">{{ news.content }}</p>
              <div class="news-meta">
                <span>{{ news.publisherName }}</span>
                <span>{{ formatTime(news.createTime) }}</span>
                <span v-if="canWrite" class="news-actions">
                  <el-button link type="primary" @click.stop="openNewsForm(news)">编辑</el-button>
                  <el-button link type="danger" @click.stop="removeNews(news)">删除</el-button>
                </span>
              </div>
            </article>
          </div>
          <div class="pagination-row">
            <span>共 {{ newsTotal }} 条</span>
            <el-pagination
              background layout="prev, pager, next" :total="newsTotal"
              :page-size="newsQuery.size" :current-page="newsQuery.page" @current-change="loadNews"
            />
          </div>
        </el-tab-pane>

        <!-- ===== 校园论坛 ===== -->
        <el-tab-pane label="校园论坛" name="forum">
          <div class="filter-bar">
            <el-input
              v-model="postQuery.keyword" clearable :prefix-icon="Search" placeholder="标题 / 内容关键字"
              class="filter-keyword" @keyup.enter="loadPosts(1)" @clear="loadPosts(1)"
            />
            <el-select v-if="canWrite" v-model="postQuery.status" clearable placeholder="全部状态" class="filter-select" @change="loadPosts(1)">
              <el-option label="正常" :value="1" />
              <el-option label="已封禁" :value="-1" />
              <el-option label="已删除" :value="0" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="loadPosts(1)">查询</el-button>
            <span class="filter-spacer"></span>
            <el-button type="primary" :icon="EditPen" @click="postFormVisible = true">发帖</el-button>
          </div>

          <div v-loading="postLoading" class="post-list">
            <el-empty v-if="!postLoading && postRows.length === 0" description="暂无帖子" />
            <article v-for="post in postRows" :key="post.postId" class="post-item" @click="openPostDetail(post)">
              <div class="post-main">
                <div class="post-title-line">
                  <strong>{{ post.title }}</strong>
                  <el-tag v-if="post.status === -1" type="danger" size="small" effect="plain">已封禁</el-tag>
                  <el-tag v-else-if="post.status === 0" type="info" size="small" effect="plain">已删除</el-tag>
                </div>
                <p class="post-summary">{{ post.content }}</p>
                <div class="post-meta">
                  <span>{{ post.authorName }}</span>
                  <span>{{ formatTime(post.createTime) }}</span>
                </div>
              </div>
              <div class="post-stats">
                <span><el-icon><View /></el-icon>{{ post.viewCount }}</span>
                <span><el-icon><Pointer /></el-icon>{{ post.likeCount }}</span>
                <span><el-icon><ChatDotRound /></el-icon>{{ post.commentCount }}</span>
              </div>
            </article>
          </div>
          <div class="pagination-row">
            <span>共 {{ postTotal }} 个帖子</span>
            <el-pagination
              background layout="prev, pager, next" :total="postTotal"
              :page-size="postQuery.size" :current-page="postQuery.page" @current-change="loadPosts"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>

    <!-- 新闻详情 -->
    <el-dialog v-model="newsDetailVisible" :title="activeNews?.title" width="min(680px, calc(100vw - 32px))">
      <div class="news-detail-meta">
        <el-tag size="small" effect="plain" :type="activeNews?.newsType === '公告' ? 'warning' : 'info'">{{ activeNews?.newsType }}</el-tag>
        <span>{{ activeNews?.publisherName }}</span>
        <span>{{ formatTime(activeNews?.createTime) }}</span>
      </div>
      <p class="news-detail-content">{{ activeNews?.content }}</p>
    </el-dialog>

    <!-- 新闻发布/编辑 -->
    <el-dialog v-model="newsFormVisible" :title="newsForm.newsId ? '编辑内容' : '发布新闻公告'" width="min(640px, calc(100vw - 32px))">
      <el-form ref="newsFormRef" :model="newsForm" :rules="newsRules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="newsForm.title" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="类型" prop="newsType">
          <el-radio-group v-model="newsForm.newsType">
            <el-radio value="公告">公告</el-radio>
            <el-radio value="新闻">新闻</el-radio>
          </el-radio-group>
          <el-checkbox v-model="newsForm.isPinned" :true-value="1" :false-value="0" class="pin-check">置顶显示</el-checkbox>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="newsForm.content" type="textarea" :rows="8" maxlength="5000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="newsFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveNews">发布</el-button>
      </template>
    </el-dialog>

    <!-- 发帖 -->
    <el-dialog v-model="postFormVisible" title="发布帖子" width="min(640px, calc(100vw - 32px))">
      <el-form ref="postFormRef" :model="postForm" :rules="postRules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="postForm.title" maxlength="128" show-word-limit placeholder="用一句话描述你的话题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="postForm.content" type="textarea" :rows="8" maxlength="5000" show-word-limit placeholder="友善交流，遵守社区规范" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="postFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="savePost">发布</el-button>
      </template>
    </el-dialog>

    <!-- 帖子详情抽屉 -->
    <el-drawer v-model="postDetailVisible" :title="activePost?.title" size="min(620px, 94vw)">
      <div v-if="activePost" class="post-detail">
        <div class="post-detail-meta">
          <span>{{ activePost.authorName }}</span>
          <span>{{ formatTime(activePost.createTime) }}</span>
          <el-tag v-if="activePost.status === -1" type="danger" size="small" effect="plain">已封禁</el-tag>
        </div>
        <p class="post-detail-content">{{ activePost.content }}</p>
        <div class="post-detail-actions">
          <el-button :icon="Pointer" @click="like(activePost)">点赞 {{ activePost.likeCount }}</el-button>
          <span class="filter-spacer"></span>
          <template v-if="canWrite">
            <el-button v-if="activePost.status === 1" type="warning" plain @click="moderate(activePost, -1)">封禁</el-button>
            <el-button v-else-if="activePost.status === -1" type="success" plain @click="moderate(activePost, 1)">解封</el-button>
          </template>
          <el-button
            v-if="canWrite || activePost.authorId === currentUserId"
            type="danger" plain :icon="Delete" @click="removePost(activePost)"
          >删除帖子</el-button>
        </div>

        <h3 class="comment-heading">全部回复（{{ comments.length }}）</h3>
        <div v-loading="commentLoading" class="comment-list">
          <el-empty v-if="!commentLoading && comments.length === 0" description="还没有回复，来抢沙发" :image-size="60" />
          <div v-for="comment in comments" :key="comment.commentId" class="comment-item">
            <div class="comment-head">
              <strong>{{ comment.authorName }}</strong>
              <span>{{ formatTime(comment.createTime) }}</span>
              <el-button
                v-if="canWrite || comment.authorId === currentUserId"
                link type="danger" @click="removeComment(comment)"
              >删除</el-button>
            </div>
            <p>{{ comment.content }}</p>
          </div>
        </div>

        <div v-if="activePost.status === 1" class="comment-editor">
          <el-input v-model="commentInput" type="textarea" :rows="3" maxlength="1000" placeholder="写下你的回复…" />
          <el-button type="primary" :loading="saving" :disabled="!commentInput.trim()" @click="submitComment">回复</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowRight, ChatDotRound, Delete, EditPen, Plus, Pointer, Search, View,
} from '@element-plus/icons-vue'
import {
  addForumComment, addForumPost, addNews, delForumComment, delForumPost, delNews,
  getForumPost, likeForumPost, listForumComments, listForumPostPage, listNewsPage,
  moderateForumPost, updateNews,
} from '@/api/base.js'
import { getStoredCurrentUser } from '@/utils/authSession.js'
import './base-d.css'

const storedUser = getStoredCurrentUser()
const canWrite = computed(() => (storedUser?.permissions || []).includes('base:write'))
const currentUserId = storedUser?.user?.userId ?? null

const activeTab = ref('news')
const saving = ref(false)
const formatTime = (time) => (time ? String(time).replace('T', ' ').slice(0, 16) : '')

// ===== 新闻公告 =====
const newsLoading = ref(false)
const newsRows = ref([])
const newsTotal = ref(0)
const newsQuery = reactive({ page: 1, size: 10, keyword: '', newsType: null })
const newsDetailVisible = ref(false)
const activeNews = ref(null)

const loadNews = async (page) => {
  if (page) newsQuery.page = page
  newsLoading.value = true
  try {
    const res = await listNewsPage({ ...newsQuery })
    newsRows.value = res.data.records
    newsTotal.value = Number(res.data.total)
  } finally {
    newsLoading.value = false
  }
}

const openNewsDetail = (news) => {
  activeNews.value = news
  newsDetailVisible.value = true
}

const newsFormVisible = ref(false)
const newsFormRef = ref(null)
const newsForm = reactive({ newsId: null, title: '', content: '', newsType: '公告', isPinned: 0 })
const newsRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  newsType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

const openNewsForm = (news) => {
  Object.assign(newsForm, news
    ? { newsId: news.newsId, title: news.title, content: news.content, newsType: news.newsType, isPinned: news.isPinned }
    : { newsId: null, title: '', content: '', newsType: '公告', isPinned: 0 })
  newsFormVisible.value = true
}

const saveNews = async () => {
  await newsFormRef.value.validate()
  saving.value = true
  try {
    if (newsForm.newsId) {
      await updateNews(newsForm.newsId, newsForm)
    } else {
      await addNews(newsForm)
    }
    ElMessage.success('发布成功')
    newsFormVisible.value = false
    loadNews()
  } finally {
    saving.value = false
  }
}

const removeNews = async (news) => {
  await ElMessageBox.confirm(`确定删除「${news.title}」吗？`, '删除确认', { type: 'warning' })
  await delNews(news.newsId)
  ElMessage.success('删除成功')
  loadNews()
}

// ===== 校园论坛 =====
const postLoading = ref(false)
const postRows = ref([])
const postTotal = ref(0)
const postQuery = reactive({ page: 1, size: 10, keyword: '', status: null })

const loadPosts = async (page) => {
  if (page) postQuery.page = page
  postLoading.value = true
  try {
    const res = await listForumPostPage({ ...postQuery })
    postRows.value = res.data.records
    postTotal.value = Number(res.data.total)
  } finally {
    postLoading.value = false
  }
}

const postFormVisible = ref(false)
const postFormRef = ref(null)
const postForm = reactive({ title: '', content: '' })
const postRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

const savePost = async () => {
  await postFormRef.value.validate()
  saving.value = true
  try {
    await addForumPost({ ...postForm })
    ElMessage.success('发布成功')
    postFormVisible.value = false
    postForm.title = ''
    postForm.content = ''
    loadPosts(1)
  } finally {
    saving.value = false
  }
}

// ===== 帖子详情与回复 =====
const postDetailVisible = ref(false)
const activePost = ref(null)
const comments = ref([])
const commentLoading = ref(false)
const commentInput = ref('')

const openPostDetail = async (post) => {
  const res = await getForumPost(post.postId)
  activePost.value = { ...post, ...res.data }
  postDetailVisible.value = true
  commentInput.value = ''
  loadComments(post.postId)
}

const loadComments = async (postId) => {
  commentLoading.value = true
  try {
    const res = await listForumComments(postId)
    comments.value = res.data
  } finally {
    commentLoading.value = false
  }
}

const like = async (post) => {
  await likeForumPost(post.postId)
  post.likeCount += 1
  ElMessage.success('已点赞')
}

const submitComment = async () => {
  saving.value = true
  try {
    await addForumComment(activePost.value.postId, { content: commentInput.value.trim() })
    commentInput.value = ''
    ElMessage.success('回复成功')
    loadComments(activePost.value.postId)
  } finally {
    saving.value = false
  }
}

const removeComment = async (comment) => {
  await ElMessageBox.confirm('确定删除这条回复吗？', '删除确认', { type: 'warning' })
  await delForumComment(comment.commentId)
  ElMessage.success('删除成功')
  loadComments(activePost.value.postId)
}

const removePost = async (post) => {
  await ElMessageBox.confirm(`确定删除帖子「${post.title}」吗？`, '删除确认', { type: 'warning' })
  await delForumPost(post.postId)
  ElMessage.success('删除成功')
  postDetailVisible.value = false
  loadPosts()
}

const moderate = async (post, status) => {
  await moderateForumPost(post.postId, status)
  activePost.value.status = status
  ElMessage.success(status === -1 ? '已封禁该帖子' : '已恢复该帖子')
  loadPosts()
}

onMounted(() => {
  loadNews()
  loadPosts()
})
</script>

<style scoped>
.tab-panel { padding: 0 16px 8px; }
.filter-bar { display: flex; flex-wrap: wrap; align-items: center; gap: 10px; padding: 4px 0 14px; border-bottom: 1px solid var(--color-border-light); }
.filter-keyword { width: 220px; }
.filter-select { width: 130px; }
.filter-spacer { flex: 1; }
.pagination-row { display: flex; align-items: center; justify-content: space-between; padding: 12px 0; color: var(--color-text-tertiary); font-size: 12px; }

.news-item { padding: 14px 4px; border-bottom: 1px solid var(--color-border-light); cursor: pointer; }
.news-item:hover .news-title-line strong { color: var(--d-accent); }
.news-title-line { display: flex; align-items: center; gap: 8px; }
.news-title-line strong { font-size: 15px; font-weight: 600; transition: color 0.15s ease; }
.news-summary { display: -webkit-box; overflow: hidden; -webkit-box-orient: vertical; -webkit-line-clamp: 2; margin: 6px 0; color: var(--color-text-secondary); font-size: 13px; }
.news-meta { display: flex; align-items: center; gap: 16px; color: var(--color-text-tertiary); font-size: 12px; }
.news-actions { margin-left: auto; }
.news-detail-meta { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; color: var(--color-text-tertiary); font-size: 13px; }
.news-detail-content { margin: 0; line-height: 1.85; white-space: pre-wrap; }

.post-item { display: flex; gap: 20px; padding: 14px 4px; border-bottom: 1px solid var(--color-border-light); cursor: pointer; }
.post-item:hover .post-title-line strong { color: var(--d-accent); }
.post-main { min-width: 0; flex: 1; }
.post-title-line { display: flex; align-items: center; gap: 8px; }
.post-title-line strong { font-size: 15px; font-weight: 600; transition: color 0.15s ease; }
.post-summary { display: -webkit-box; overflow: hidden; -webkit-box-orient: vertical; -webkit-line-clamp: 2; margin: 6px 0; color: var(--color-text-secondary); font-size: 13px; }
.post-meta { display: flex; gap: 16px; color: var(--color-text-tertiary); font-size: 12px; }
.post-stats { display: flex; flex-direction: column; justify-content: center; gap: 6px; color: var(--color-text-tertiary); font-size: 12px; }
.post-stats span { display: flex; align-items: center; gap: 4px; font-variant-numeric: tabular-nums; }

.post-detail-meta { display: flex; align-items: center; gap: 12px; color: var(--color-text-tertiary); font-size: 13px; }
.post-detail-content { margin: 14px 0; line-height: 1.85; white-space: pre-wrap; }
.post-detail-actions { display: flex; align-items: center; gap: 10px; padding: 12px 0; border-top: 1px solid var(--color-border-light); border-bottom: 1px solid var(--color-border-light); }
.comment-heading { margin: 18px 0 6px; font-size: 15px; font-weight: 600; }
.comment-item { padding: 12px 0; border-bottom: 1px solid var(--color-border-light); }
.comment-head { display: flex; align-items: center; gap: 12px; font-size: 13px; }
.comment-head strong { font-weight: 600; }
.comment-head span { color: var(--color-text-tertiary); font-size: 12px; }
.comment-head .el-button { margin-left: auto; }
.comment-item p { margin: 6px 0 0; font-size: 13px; line-height: 1.7; white-space: pre-wrap; }
.comment-editor { display: flex; align-items: flex-end; gap: 10px; margin-top: 16px; }
.comment-editor .el-input { flex: 1; }
.pin-check { margin-left: 24px; }
@media (max-width: 767px) {
  .filter-keyword { width: 100%; }
  .post-stats { flex-direction: row; }
  .post-item { flex-direction: column; gap: 8px; }
}
</style>
