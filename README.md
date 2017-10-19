# PullToRefreshLayout
Android下拉刷新控件，功能和使用方法和系统的SwipeRefreshLayout完全一致，可以自定义头部布局。

### 添加headView的3种方法(建议不要混合使用)：
1. 使用自定义属性： app:ptr_head_layout="@layout/pull_down_header",此时布局中第一个子View将是内容。
2. 直接将下拉头部View写在布局中，和FrameLayout一样的布局方式。注意，布局中第一个子View为下拉头，第二个为内容
3. 在布局中直接指定headView和内容：分别将需要设置成headView和内容布局的tag设置为"headView"和"targetView"即可
- 设置下拉刷新监听：setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener)
- 展示、隐藏头部刷新布局setRefreshing(boolean)
- 禁用下拉setEnabled(boolean)
- 自定义下拉触发条件：setScrollDownCallBack()
