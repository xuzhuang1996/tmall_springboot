## 接口定义
1. AutoReloadCache<K, V>作为一切缓存的抽象类，成员：
   - `Map<K,V>cache`,（需要考虑。如果数据库没有数据，该怎么返回）
   - `initialed`，是否初始化。如果没有，作为首次访问数据，加载数据
   - `Map<K,V>reload()`方法,直接从数据库加载数据。作为具体加载方式，由子类完成重写任务。
   - `Map<K,V>get()`方法,返回当前缓存数据，类似，`V get(K key)`,根据key来获取缓存内容
   - `getDelay()`方法，默认间隔多少次从数据库取数据
   - `checkAndStartTimer()`方法，如果没有初始化，启动线程。利用newSingleThreadScheduledExecutor线程池的scheduleWithFixedDelay方法来完成reload的任务。
      - scheduleWithFixedDelay的几个参数：
        - Runnable command,要执行的任务
        - long initialDelay,首次执行的延迟时间
        - delay ,一次执行终止和下一次执行开始之间的延迟,getDelay()
        - unit,initialDelay 和 delay 参数的时间单位,TimeUnit.SECONDS
      - scheduleWithFixedDelay该方法描述：创建并执行一个在给定初始延迟后首次启用的定期操作，随后，在每一次执行终止和下一次执行开始之间都存在给定的延迟。如果任务的任一执行遇到异常，就会取消后续执行。否则，只能通过执行程序的取消或终止方法来终止该任务。
      - newSingleThreadScheduledExecutor可以在构造函数中传入ThreadFactory，自己可以定义一个CacheThreadFactory,在其构造函数中传入函数的名字，以及定义异常处理等（先加入线程名）
      
2. 定义CacheService服务接口,根据具体业务，加入具体业务的接口方法。在其实现类中，注入具体DAO,主要在于：成员变量为具体实现缓存的匿名类对象，也就是AutoReloadCache<K, V>的对象，并在里面自行完成reload()方法的重写，根据不同业务，重写加载函数。
3. 使用方式：在具体业务的service类中，调用CacheService中定义的各种具体业务方法（２中已重写reload方法，在该方法中完成对数据的读）。
      
