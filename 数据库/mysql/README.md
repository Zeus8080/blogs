

**TRUNCATE TABLE table;**

清空表，也会清空自增的主键，一般用在主键不是从1开始时可以用下



**LAST\_**_**INSERT\_ID**_**两种用法**

```
create table test{
    id long primary key,
    name varchar(20);
}

insert into test (name) value("111");
insert into test (name) value("www");
表结构：
id    name
1     111
2     www

执行sql：select last_insert_id();    
结果：2
说明：不带参数时查询上次插入的主键自增id

执行sql：select last_inser_id(2+1);
结果：3
说明：带参数，相当于执行里面的的表达式
```







