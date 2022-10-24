# bitlap

> 龟速开发中，感兴趣的可以star一下

| Project Stage | CI              | Codecov                                   |
|---------------|-----------------|-------------------------------------------|
| ![Stage]      | ![CI][Badge-CI] | [![codecov][Badge-Codecov]][Link-Codecov] |


## 架构

![](http://ice-img.flutterdart.cn/2021-08-01-165808.png)

## 通过client查询

### Scala例子

***添加依赖***
```scala
libraryDependencies += (
  "org.bitlap" % "bitlap-client" % "0.1.0-SNAPSHOT",
  "org.bitlap" % "smt-common" % "0.9.0"
)

// 若运行时报错 找不到grpc某个类的话，加这几行试试，保证io.grpc的所有组件都是一个版本！
dependencyOverrides ++= Seq(
  "io.grpc" % "grpc-core" % "1.46.0",
  "io.grpc" % "grpc-netty" % "1.46.0"
)
```
***使用***
```scala
Class.forName(classOf[org.bitlap.Driver].getName)
val statement = DriverManager.getConnection("jdbc:bitlap://localhost:23333/default").createStatement()
statement.execute(
  s"""select _time, sum(vv) as vv, sum(pv) as pv, count(distinct pv) as uv
     |from table_test
     |where _time >= 0
     |group by _time""".stripMargin)

val rowSet: ResultSet = statement.getResultSet

// ResultSetTransformer是个工具类，会把ResultSet提取为Seq，其中GenericRow4表示结果是四列，每个类型需要指定，五列就是GenericRow5，以此类推。
val ret1: Seq[GenericRow4[Long, Double, Double, Long]] = ResultSetTransformer[GenericRow4[Long, Double, Double, Long]].toResults(rowSet)
println(ret1)
```

### Java例子

***添加依赖***
```xml
<dependency>
    <groupId>org.bitlap</groupId>
    <artifactId>bitlap-client</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```
***使用***
```java
public class JavaServerTest {

    static {
        try {
            Class.forName(org.bitlap.Driver.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    final String table = "test_table";

    @BeforeClass
    public static void startServer() throws InterruptedException, SQLException {
        Thread server = new Thread(() -> EmbedBitlapServer.main(new String[0]));
        server.setDaemon(true);
        server.start();
        Thread.sleep(3000L);
        initTable();
    }

    private static void initTable() throws SQLException {
        Statement stmt = conn().createStatement();
        stmt.execute("create table if not exists $table");
        stmt.execute("load data 'classpath:simple_data.csv' overwrite table $table"); // load的是server模块的csv
    }

    public static Connection conn() throws SQLException {
        return DriverManager.getConnection("jdbc:bitlap://localhost:23333/default");
    }

    public static class Tuple4 {
        private Long col1;
        private Double col2;
        private Double col3;
        private Long col4;

        public Tuple4(Long col1, Double col2, Double col3, Long col4) {
            this.col1 = col1;
            this.col2 = col2;
            this.col3 = col3;
            this.col4 = col4;
        }
    }

    @Test
    public void query_test1() throws SQLException {
        Statement stmt = conn().createStatement();
        stmt.setMaxRows(10);
        stmt.execute("select _time, sum(vv) as vv, sum(pv) as pv, count(distinct pv) as uv " + "  from " + table + "   where _time >= 0 " + " group by _time");
        ResultSet rs = stmt.getResultSet();
        List<Tuple4> ret = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                ret.add(new Tuple4(rs.getLong("_time"), rs.getDouble("vv"), rs.getDouble("pv"), rs.getLong("uv")));
            }
        }

        assert ret.size() > 0;
    }
}
```


## 如何贡献

[CONTRIBUTING](./CONTRIBUTING.md)

## 许可

```
MIT License

Copyright (c) 2022 bitlap.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

[Stage]: https://img.shields.io/badge/Project%20Stage-Development-yellowgreen.svg

[Badge-CI]: https://github.com/bitlap/bitlap/actions/workflows/ci.yml/badge.svg

[Badge-Maven]: https://img.shields.io/maven-central/v/org.bitlap/bitlap

[Badge-Discord]: https://img.shields.io/discord/968687999862841384

[Badge-Codecov]: https://codecov.io/gh/bitlap/bitlap/branch/dev/graph/badge.svg?token=9XJ2LC2K8M

[Badge-Snapshots]: https://img.shields.io/nexus/s/org.bitlap/bitlap-core?server=https%3A%2F%2Fs01.oss.sonatype.org

[Link-Discord]: https://discord.com/invite/vp5stpz6eU

[Link-Codecov]: https://codecov.io/gh/bitlap/bitlap

[Link-Maven]: https://search.maven.org/search?q=g:%22org.bitlap%22%20AND%20a:%22bitlap%22

[Link-Snapshots]: https://s01.oss.sonatype.org/content/repositories/snapshots/org/bitlap/
