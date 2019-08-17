## 进度
1. 2019/8/4：@Builder注解完成：私有构造函数增强完成。接下来注入Builder静态类
2. 2019/8/10:[准备参考](https://blog.csdn.net/dap769815768/article/details/90448451)


## 实现步骤：
1. 新建Lombok工程，新建myBuilder注解，

        //该注解主要是为了增强被修饰的类：Builder建筑者模式
        //ElementType.TYPE用于描述类、接口(包括注解类型) 或enum声明
        @Target({ ElementType.TYPE })
        @Inherited
        @Retention(RetentionPolicy.RUNTIME)
        public @interface myBuilder {
        } 
2. 定义BeanBuilderProcessor类，用于处理该注解，具体直接看代码。完成好编码工作后。
3. 配置：
   1. Lombok工程中，在pom.xml中需要添加依赖，以及编译时的配置
   
               <build>
                    <--必须添加这个，同时在resource下的META-INF中新增javax.annotation.processing.Processor文件，里面写入用于处理注解的处理类BeanBuilderProcessor-->
                    <resources>
                        <resource>
                            <directory>src/main/resources</directory>
                            <excludes>
                                <exclude>META-INF/**/*</exclude>
                            </excludes>
                        </resource>
                    </resources>
                    <plugins>
                        <!--这个自带的要删掉，如果是springboot工程，就需要删掉-->
                        <!--<plugin>-->
                            <!--<groupId>org.springframework.boot</groupId>-->
                            <!--<artifactId>spring-boot-maven-plugin</artifactId>-->
                        <!--</plugin>-->
                        <!--然后主要是下面的编译配置-->
                        <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-resources-plugin</artifactId>
                            <version>2.6</version>
                            <executions>
                                <execution>
                                    <id>process-META</id>
                                    <phase>prepare-package</phase>
                                    <goals>
                                        <goal>copy-resources</goal>
                                    </goals>
                                    <configuration>
                                        <outputDirectory>target/classes</outputDirectory>
                                        <resources>
                                            <resource>
                                                <directory>${basedir}/src/main/resources/</directory>
                                                <includes>
                                                    <include>**/*</include>
                                                </includes>
                                            </resource>
                                        </resources>
                                    </configuration>
                                </execution>
                            </executions>
                        </plugin>
                    </plugins>
                </build>
    2. Lombok工程的resources/META-INF.services中新建文件，添加处理类的完整包名。这样的目的：Lombok工程在启动的时候根据文件中配置的 Jar 包去扫描项目所依赖的 Jar 包
    
                com.xu.lombok.processor.BeanBuilderProcessor
                com.xu.lombok.processor.AllArgsConstructorProcessor
                com.xu.lombok.processor.BeanLogProcessor
    2. Lombok工程的pom.xml还需要添加依赖。
            <dependency>
                <groupId>com.sun</groupId>
                <artifactId>tools</artifactId>
                <version>1.8</version>
                <scope>system</scope>
                <systemPath>${java.home}\..\lib\tools.jar</systemPath><!--这个就是找到jdk/lib包下的tools.jar，需要这个依赖-->
            </dependency>
            
    4. 然后maven打包进本地私服：左上角的maven-Lifecycle-具体工程-clean-install
    3. 而使用该注解的工程，只需要在自己工程的pom.xml添加对上面工程的依赖就可以了：
    
            <dependency>
                <groupId>com.xu</groupId>
                <artifactId>lombok</artifactId>
                <version>0.0.1-SNAPSHOT</version>
            </dependency>
     5. 如果想在其中打印信息，我发现message没有打印出来。不过编译期间sout倒是打出来了。
3. 在完成这些后，可以直接使用注解所带来的增加，如增加了一个方法，可以直接调用，但是idea没有插件能识别，需要自己写插件来完成。但是这不影响我们直接调用，也就是说，完成上面的工作后就可以直接运行。

4. 参考
   1. [jctree语法](https://static.javadoc.io/org.kohsuke.sorcerer/sorcerer-javac/0.11/com/sun/tools/javac/tree/JCTree.html)
   1. [JSR语法博客](https://blog.csdn.net/a_zhenzhen/article/details/86065063)
   1. [AST类创建语法](https://blog.csdn.net/lovelion/article/details/20309379) 
   2. [配置](https://juejin.im/entry/5a390ba76fb9a0451e3fed7c)
   3. [set方法参数问题](https://nicky-chen.github.io/2019/05/03/apt_lombok_implement/)
   4. [构造函数增强](https://houbb.github.io/2017/10/13/jctree)
   5. [最终参考](https://liuyehcf.github.io/2018/02/02/Java-JSR-269-%E6%8F%92%E5%85%A5%E5%BC%8F%E6%B3%A8%E8%A7%A3%E5%A4%84%E7%90%86%E5%99%A8/) 
