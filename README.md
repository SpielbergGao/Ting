> 我是听书重度用户，因为某喜下架了很多我喜欢听的免费资源，，比如《我当算命先生那几年》(支持正版，我也经常付费听的，但是这个版本确实是免费的，但是被下架了，特别想听)，所以在网上找了一个源，自己提出的需求，做了这款app

# 1. 声明：播放源来自网上，采用jsoup抓取，学习用途

# 2. 项目效果图
### 主界面：主要是搜索功能，热门搜索目前是写死的
![图片描述](https://user-gold-cdn.xitu.io/2019/6/2/16b15a4b525bbdf1?w=278&h=581&f=png&s=26417)
### 搜索结果页面：支持下拉刷新，上拉加载更多，滑动到第2页提供返回顶部按钮
![图片描述](https://user-gold-cdn.xitu.io/2019/6/2/16b15a4b4f8082ff?w=257&h=505&f=png&s=40984)
### 作品详情页面：头部有该作品搜索记录，头部下方是该作品的集数
![图片描述](https://user-gold-cdn.xitu.io/2019/6/2/16b15a4b502e92f5?w=287&h=599&f=png&s=65227)
### 播放页：
当播放源是有效的时候，会弹出绿色Toast提醒,自动播放
![图片描述](https://user-gold-cdn.xitu.io/2019/6/2/16b15a4b4f6a7b8a?w=295&h=604&f=png&s=63208)
有时候播放源url是无效的，如下图，弹出黄色Toast提醒，则表明目前源是无效的，app会自动切源，一般当这个红色部分如44自动尝试到60的时候（请稍微耐心等待，到60即可播放）就能切到能播放的源，自动播放
![图片描述](https://user-gold-cdn.xitu.io/2019/6/2/16b15a4b537d24f7?w=298&h=594&f=png&s=35031)

### 听书记录页：离开播放页面后存储听书记录
![图片描述](https://user-gold-cdn.xitu.io/2019/6/2/16b15a4b4f4efcdd?w=291&h=614&f=png&s=41340)

### 状态栏播放控制：app处于后台时，提供通知栏UI控制播放（上一集，播放/暂停，下一集）
![图片描述](https://user-gold-cdn.xitu.io/2019/6/2/16b15a4b83c6af5c?w=294&h=603&f=png&s=39269)

# 3. Apk下载地址
[https://github.com/zjw-swun/Ting/blob/master/app-debug.apk](https://github.com/zjw-swun/Ting/blob/master/app-debug.apk)

# 项目地址
[https://github.com/zjw-swun/Ting](https://github.com/zjw-swun/Ting)