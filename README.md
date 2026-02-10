# Spring Boot ロギング入門 - アクセスログ・業務ログを集める

## Spring Initializr

[これ](https://start.spring.io/#!type=maven-project&language=java&platformVersion=4.0.2&packaging=jar&configurationFileFormat=yaml&jvmVersion=21&groupId=dev.mikoto2000.springboot&artifactId=logging&name=logging&description=Logging%20demo%20project%20for%20Spring%20Boot&packageName=dev.mikoto2000.springboot.logging&dependencies=devtools,lombok,web)

## アプリの作成

このアプリでは、簡易的なユーザー管理APIを用意します。

- `/addUser`: ユーザーを追加する
- `/removeUser`: ユーザーを削除する
- `/getUsers`: 登録済みユーザー一覧を取得する


### サービスの作成

```java
package dev.mikoto2000.springboot.logging.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

/**
 * MiscService
 */
@Service
public class MiscService {

  private final Set<String> users = new HashSet<>();

  public void addUser(String name) {
    users.add(name);
  }

  public void removeUser(String name) {
    users.remove(name);
  }

  public Set<String> getUsers() {
    return new HashSet<String>(users);
  }
}
```

### コントローラーの作成

`src/main/java/dev/mikoto2000/springboot/logging/controller/MiscController.java`:

```java
package dev.mikoto2000.springboot.logging.controller;

import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.mikoto2000.springboot.logging.service.MiscService;
import lombok.RequiredArgsConstructor;

/**
 * MiscController
 */
@RequiredArgsConstructor
@RestController
public class MiscController {

  private final MiscService service;

  @GetMapping("addUser")
  public void addUser(
      @RequestParam String name
      ) {
    service.addUser(name);
  }

  @GetMapping("removeUser")
  public void removeUser(
      @RequestParam String name
      ) {
    service.removeUser(name);
  }

  @GetMapping("getUsers")
  public Set<String> getUsers() {
    return service.getUsers();
  }
}
```

## アクセスログの追加

各エンドポイントにアクセスしたことを記録する、アクセスログを追加します。

### アクセスログ用フィルタの作成

`src/main/java/dev/mikoto2000/springboot/logging/filter/AccessLogFilter.java`:

```java
package dev.mikoto2000.springboot.logging.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AccessLogFilter extends OncePerRequestFilter {

    private static final Logger accessLogger = LoggerFactory.getLogger("ACCESS_LOG");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();

        // 接続元 IP 取得
        String ip = request.getRemoteAddr();

        // 成功・失敗フラグ
        boolean success = false;

        try {
            chain.doFilter(request, response);
            success = true;
        } finally {

            long time = System.currentTimeMillis() - start;

            accessLogger.info("ip={}, method={}, request_url={}, status={} success={}, time={}ms",
                ip,
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                success ? "SUCCESS" : "FAIL",
                time);
        }
    }
}
```

### アクセスログの動作確認

curl コマンドで、それぞれのエンドポイントにアクセスしてみましょう。
ついでに存在しないエンドポイントにもアクセスしてみます。

```sh
curl http://localhost:8080/addUser?name=mikoto2000
curl http://localhost:8080/getUsers
curl http://localhost:8080/removeUser?name=mikoto2000
curl http://localhost:8080/invalidEndpoint
```

次のようなログが出力されます。

```
2026-02-10T10:33:28.818Z  INFO 9018 --- [logging] [nio-8080-exec-2] ACCESS_LOG : ip=127.0.0.1, method=GET, request_url=/addUser, status=200, success=SUCCESS, time=1ms
2026-02-10T10:33:28.832Z  INFO 9018 --- [logging] [nio-8080-exec-3] ACCESS_LOG : ip=127.0.0.1, method=GET, request_url=/getUsers, status=200, success=SUCCESS, time=1ms
2026-02-10T10:33:28.844Z  INFO 9018 --- [logging] [nio-8080-exec-5] ACCESS_LOG : ip=127.0.0.1, method=GET, request_url=/removeUser, status=200, success=SUCCESS, time=1ms
2026-02-10T10:33:28.855Z  INFO 9018 --- [logging] [nio-8080-exec-7] ACCESS_LOG : ip=127.0.0.1, method=GET, request_url=/invalidEndpoint, status=404, success=SUCCESS, time=1ms
```

- `ip`: アクセス元の IP アドレス ※実務では、プロキシ配下等の場合に IP の取得方法にひと工夫が必要です（今回は割愛します）
- `method`: HTTP メソッド（GET / POST など）
- `request_url`: アクセスされたパス
- `status`: HTTP ステータスコード（200 / 404 など）
- `success`: アプリケーションが例外なく処理を完了したかどうか ※ 「HTTP ステータス」の「成功」ではなく、「アプリの内部処理の成否」であることに注意
- `time`: 処理にかかった時間（ミリ秒）

また、注目してほしいのは 404 で失敗しているものもちゃんとログに記録されているところです。
Filter でアクセスログを取得しているため、コントローラーが呼ばれない場合でも記録できます。


