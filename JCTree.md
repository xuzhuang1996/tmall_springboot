## 进度
1. 2019/8/4：@Builder注解完成：私有构造函数增强完成。接下来注入Builder静态类


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
    2. Lombok工程的pom.xml还需要添加依赖。然后maven打包进本地私服：左上角的maven-Lifecycle-具体工程-clean-install
  
            <dependency>
                <groupId>com.sun</groupId>
                <artifactId>tools</artifactId>
                <version>1.8</version>
                <scope>system</scope>
                <systemPath>${java.home}\..\lib\tools.jar</systemPath>
            </dependency>
            
            
    3. 而使用该注解的工程，只需要在自己工程的pom.xml添加对上面工程的依赖就可以了：
    
            <dependency>
                <groupId>com.xu</groupId>
                <artifactId>lombok</artifactId>
                <version>0.0.1-SNAPSHOT</version>
            </dependency>
            
3. 在完成这些后，可以直接使用注解所带来的增加，如增加了一个方法，可以直接调用，但是idea没有插件能识别，需要自己写插件来完成。但是这不影响我们直接调用，也就是说，完成上面的工作后就可以直接运行。

4. 参考
[jctree语法](https://blog.csdn.net/u013998373/article/details/90050810#JCExpression_338)
[配置](https://juejin.im/entry/5a390ba76fb9a0451e3fed7c)
[set方法参数问题](https://nicky-chen.github.io/2019/05/03/apt_lombok_implement/)
[构造函数增强](https://houbb.github.io/2017/10/13/jctree)
