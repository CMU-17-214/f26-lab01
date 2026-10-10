第一轮：获取实现思路（**不要让AI写代码**）
我们实现会议室预订系统的cancelBooking(String bookingId)方法，请严格遵循R1‑R6业务规则：
R1：方法签名 public void cancelBooking(String bookingId)
R2：删除传入id对应的已确认预订，释放时间段。
R3：传入不存在的预订ID，不抛出异常，不做任何操作，也不触发候补晋升。
R4：取消成功后处理该房间候补名单，候补用户按照seq从小到大排序，遵循先到先得。
R5：每次取消操作最多只能晋升1位候补用户。
R6：晋升判断时间冲突，要和该房间全部现存已确认预订做overlaps冲突判断，不能只对比刚刚被删除的预订；晋升逻辑禁止调用book()，调用book会把用户再次加入候补队列。

请输出cancelBooking执行步骤与实现思路，**不要输出任何Java代码**。

 第二轮：基于思路生成Java代码
基于刚刚输出的实现思路，在BookingService类中编写cancelBooking(String bookingId)完整Java代码。
继续严格遵守R1‑R6全部规则，晋升不能调用book()方法。直接输出完整代码。

第三轮：让AI自查代码漏洞
检查你刚刚写的cancelBooking代码，逐条对照R1、R2、R3、R4、R5、R6规则，找出代码里面违反业务规则的bug，列出所有问题点。