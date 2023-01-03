- **品質担保**  
[![sync-pr](https://github.com/begyyal/tumeshogi_solver/actions/workflows/sync-pr.yml/badge.svg)](https://github.com/begyyal/tumeshogi_solver/actions/workflows/sync-pr.yml)
[![push-stg](https://github.com/begyyal/tumeshogi_solver/actions/workflows/push-stg.yml/badge.svg)](https://github.com/begyyal/tumeshogi_solver/actions/workflows/push-stg.yml)  

- **開発補助**  
[![create-feature](https://github.com/begyyal/tumeshogi_solver/actions/workflows/create-feature.yml/badge.svg)](https://github.com/begyyal/tumeshogi_solve/actions/workflows/create-feature.yml)
[![delete-feature](https://github.com/begyyal/tumeshogi_solver/actions/workflows/delete-feature.yml/badge.svg)](https://github.com/begyyal/tumeshogi_solver/actions/workflows/delete-feature.yml)   

- **その他**  
[![push-tags](https://github.com/begyyal/tumeshogi_solver/actions/workflows/push-tags.yml/badge.svg)](https://github.com/begyyal/tumeshogi_solver/actions/workflows/push-tags.yml)  

# 概要

詰将棋を解くプログラムです。  
盤面及び持ち駒のデータを引数として渡すことで、詰み筋が存在する場合はその手筋を出力します。  

Javaライブラリ版と、コマンド実行形式版があります。  
詳細は各READMEをご覧ください。ここでは共通事項を記載します。  
- [Javaライブラリ(`lib/`)](./lib/README.md)
- [コマンド実行形式(`cmd/`)](./cmd/README.md)

より少ないリソースで難度の高い問題を解くことを目指していますが、  
**メモリ等の状況次第でパフォーマンスが変わる可能性があることはご留意ください。[※詳細は後述](#パフォーマンス)**


# 基本的なこと

- 複数回答がある場合も単一の回答を返します。  

- 指定手数よりも短い手数で詰み得る場合、より短い手数の回答を返します。  

- もし受け手の打ち筋に期待するものがある場合は、期待する手を入力し、  
詰み手数をその分減らした上で計算してください。

- 現状、攻め手の王は考慮されません。  

# パフォーマンス

- マシンスペック等にもよりますが、複雑な問題には相応に計算時間を要します。  
プログラムを実行してから数秒レスポンスが無くともエラーではない可能性が高いです。

- 難度相応にメモリ容量も要するため、メモリが不足している場合はOOMで落ちる可能性があります。  
アプリの処理に割り当てるメモリ容量はjavaコマンド実行時の引数等で指定が可能です。(`-Xmx`など)  
詳しくはは[Javaのドキュメント](https://docs.oracle.com/en/java/javase/15/docs/specs/man/java.html#extra-options-for-java)をご確認ください。

- 参考程度に、手元の状況を記します。随時更新します。
  - 条件
    - ヒープサイズ : 4G
    - プロセッサ : intel core i7-8650U
    - コマンド実行形式
  - 指標
    - ◎ : 最遅でも10秒以内
    - 〇 : 最遅でも1分以内
    - △ : 難度次第では1分を大幅に超過してしまう
    - ？ : 未検証
  - [v3.0.0](https://github.com/begyyal/tumeshogi_solver/releases/tag/v1) 時点
    - 7手詰め迄 : ◎
    - 9手詰め : ◎
    - 11手詰め以降 : ？
  - 最遅問題
    - 7手詰め
      - `7 55xf43xd21xaz24yh13yc xg1e1ya17b4c3d3e3f1g1`
    - 9手詰め
      - `9 14yb24yc34yc44yc54yc15yh66xg xg1e4ya18b3d4f2`
