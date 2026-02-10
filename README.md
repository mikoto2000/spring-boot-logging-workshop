# Spring Boot ロギング入門 - アクセスログ・監査ログを集める

## Spring Initializr

[これ](https://start.spring.io/#!type=maven-project&language=java&platformVersion=4.0.2&packaging=jar&configurationFileFormat=yaml&jvmVersion=21&groupId=dev.mikoto2000.springboot&artifactId=logging&name=logging&description=Logging%20demo%20project%20for%20Spring%20Boot&packageName=dev.mikoto2000.springboot.logging&dependencies=devtools,lombok,web)

## アプリの作成

`if1`, `if2`, `if3` それぞれにアクセスすると、 `1`, `2`, `3` が返却されるアプリを作成します。

### サービスの作成

```java
package dev.mikoto2000.springboot.logging.service;

import org.springframework.stereotype.Service;

/**
 * MiscService
 */
@Service
public class MiscService {
  public String service1() {
    return "1";
  }

  public String service2() {
    return "2";
  }

  public String service3() {
    return "3";
  }
}
```

### コントローラーの作成

`src/main/java/dev/mikoto2000/springboot/logging/controller/MiscController.java`:

```java
package dev.mikoto2000.springboot.logging.controller;

import org.springframework.web.bind.annotation.GetMapping;
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

  @GetMapping("if1")
  public String if1() {
    return service.service1();
  }

  @GetMapping("if2")
  public String if2() {
    return service.service2();
  }

  @GetMapping("if3")
  public String if3() {
    return service.service3();
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
curl http://localhost:8080/if1
curl http://localhost:8080/if2
curl http://localhost:8080/if3
curl http://localhost:8080/if4
```

次のようなログが出力されます。

```
2026-02-10T09:46:35.433Z  INFO 2737 --- [logging] [nio-8080-exec-1] ACCESS_LOG : ip=127.0.0.1, method=GET, request_url=/if1, status=200 success=SUCCESS, time=4ms
2026-02-10T09:46:35.459Z  INFO 2737 --- [logging] [nio-8080-exec-2] ACCESS_LOG : ip=127.0.0.1, method=GET, request_url=/if2, status=200 success=SUCCESS, time=2ms
2026-02-10T09:46:35.472Z  INFO 2737 --- [logging] [nio-8080-exec-3] ACCESS_LOG : ip=127.0.0.1, method=GET, request_url=/if3, status=200 success=SUCCESS, time=0ms
2026-02-10T09:46:47.861Z  INFO 2737 --- [logging] [nio-8080-exec-4] ACCESS_LOG : ip=127.0.0.1, method=GET, request_url=/if4, status=404 success=SUCCESS, time=8ms
```

- `ip`: アクセス元の IP アドレス ※実務では、プロキシ配下等の場合に IP の取得方法にひと工夫が必要です（今回は割愛します）
- `method`: HTTP メソッド（GET / POST など）
- `request_url`: アクセスされたパス
- `status`: HTTP ステータスコード（200 / 404 など）
- `success`: アプリケーションが例外なく処理を完了したかどうか
- `time`: 処理にかかった時間（ミリ秒）

また、注目してほしいのは 404 で失敗しているものもちゃんとログに記録されているところです。
Filter でアクセスログを取得しているため、コントローラーが呼ばれない場合でも記録できます。


