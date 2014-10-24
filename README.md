# dbtest-standalone

## 前提条件

* JDK 8

## 動作確認環境

* PostgreSQL
 * 9.2.4
 * 9.2.6
 * 9.3.5

## 利用方法

### test データベース作成

`test` ユーザが `test` データベースの作成者, 所有者としています。

~~~ sh
#!/bin/sh
createuser -d test
createdb --owner test test
~~~

### test データベースの DDL

~~~
DROP TABLE IF EXISTS test_table;

CREATE TABLE test_table (
 id integer NOT NULL,
 value integer NOT NULL,
 PRIMARY KEY (id)
);

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO test;
~~~

### dbcp.properties の編集

`src/main/resources/dbcp.properties` を環境に応じて変更してください。

主に直すのは以下です。

~~~
url=jdbc:postgresql://localhost:5432/test
username=test
password=
~~~

### 実行

初回はライブラリのダウンロードがあるため時間がかかります。

~~~ sh
./gradlew run -Pargs="-n <request-time> -s <time>"
~~~

~~~ sh
-n (--requests) <requests> : クエリ実行回数(デフォルト 1)
-s (--sleep) <time>        : 次回クエリ実行前のスリープ時間。単位: μs(デフォルト 10000 μs)
~~~

## 詳細

以下のように実行すると、標準出力に結果が表示されます。

~~~ sh
./gradlew run -Pargs="-n 10"
:compileJava UP-TO-DATE
:processResources UP-TO-DATE
:classes UP-TO-DATE
:run
13:25:44.460 [main] INFO  dbtest.standalone.App - [DBTest begin]
13:25:44.478 [main] INFO  dbtest.standalone.App - sleep time: 10000 μs
13:25:44.542 [main] INFO  dbtest.standalone.DataSourceFactory - DataSource initialized.
13:25:44.621 [main] INFO  dbtest.standalone.App - Data cleared
13:25:44.632 [pool-1-thread-1] INFO  dbtest.standalone.InsertionInvoker - [Insert success] COUNT_TOTAL: 1, COUNT_SUCCESS: 1, COUNT_FAILURE: 0
13:25:44.654 [pool-1-thread-1] INFO  dbtest.standalone.InsertionInvoker - [Insert success] COUNT_TOTAL: 2, COUNT_SUCCESS: 2, COUNT_FAILURE: 0
[...]
13:25:44.832 [pool-1-thread-1] INFO  dbtest.standalone.InsertionInvoker - [Insert success] COUNT_TOTAL: 10, COUNT_SUCCESS: 10, COUNT_FAILURE: 0
13:25:44.869 [main] INFO  dbtest.standalone.App - Insert success: 10
13:25:44.869 [main] INFO  dbtest.standalone.App - Insert failure: 0
13:25:44.869 [main] INFO  dbtest.standalone.App - Insert total: 10
13:25:44.869 [main] INFO  dbtest.standalone.App - Count Via JDBC: 10
13:25:44.877 [main] INFO  dbtest.standalone.App - Actual number of records: 10
13:25:44.878 [main] INFO  dbtest.standalone.App - [DBTest end]

BUILD SUCCESSFUL

Total time: 2.817 secs
~~~

### ログ

試験終了後は、`logs` ディレクトリに以下のようなログが出力されます。

`[YYYY-MM-DD'T'hh:mm:ss.SSS]` は試験実行時の時刻です。

ファイル名                                      | 内容
-----------------------------------------------|--------------------------------------------------
insert\_all_[YYYY-MM-DD'T'hh:mm:ss.SSS].log    | 試験の開始から終了まで全てのログ(標準出力分は含まない)
insert\_fail_[YYYY-MM-DD'T'hh:mm:ss.SSS].log   | データ挿入成功のみのログ
insert\_success_[YYYY-MM-DD'T'hh:mm:ss.SSS].log| データ挿入失敗のみのログ

### 試験サマリ

項目名                   | 内容
------------------------|-------------------------------------------
Insert success          | 挿入成功カウント数
Insert failure          | 挿入失敗カウント数
Insert total            | 全挿入カウント数
Count Via JDBC          | JDBC API のメソッドの返り値(挿入行)の集計
Actual number of records| `SELECT count(*) FROM test_table;` の結果
