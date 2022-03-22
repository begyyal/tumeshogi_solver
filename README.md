- **品質担保**  
[![Java CI with Gradle](https://github.com/begyyal/tumeshogi_solver/actions/workflows/push-develop.yml/badge.svg)](https://github.com/begyyal/tumeshogi_solver/actions/workflows/push-develop.yml)
[![Update tag](https://github.com/begyyal/tumeshogi_solver/actions/workflows/push-tags.yml/badge.svg)](https://github.com/begyyal/tumeshogi_solver/actions/workflows/push-tags.yml)

- **開発補助**  
[![Save snapshot of PR](https://github.com/begyyal/tumeshogi_solver/actions/workflows/sync-pr1.yml/badge.svg)](https://github.com/begyyal/tumeshogi_solver/actions/workflows/sync-pr1.yml)
[![Test by sync PR](https://github.com/begyyal/tumeshogi_solver/actions/workflows/sync-pr2.yml/badge.svg)](https://github.com/begyyal/tumeshogi_solver/actions/workflows/sync-pr2.yml)
[![Post processing of PR](https://github.com/begyyal/tumeshogi_solver/actions/workflows/closed-pr.yml/badge.svg)](https://github.com/begyyal/tumeshogi_solver/actions/workflows/closed-pr.yml)  
[![Create feature branch](https://github.com/begyyal/tumeshogi_solver/actions/workflows/create-feature.yml/badge.svg)](https://github.com/begyyal/tumeshogi_solver/actions/workflows/create-feature.yml)
[![Delete feature branch](https://github.com/begyyal/tumeshogi_solver/actions/workflows/delete-feature.yml/badge.svg)](https://github.com/begyyal/tumeshogi_solver/actions/workflows/delete-feature.yml)  

# 概要

詰将棋を解くプログラムです。  
盤面及び持ち駒のデータを引数として渡すことで、  
詰み筋が存在する場合はその手筋を標準出力します。  
[出力フォーマット](#出力内容)は用途によって切替が可能です。
 
より少ないリソースで難度の高い問題を解くことを目指していますが、  
**メモリ等の状況次第でパフォーマンスが変わる可能性があることはご留意ください。[※詳細は後述](#パフォーマンス)**

# 導入

1. Java15以上の実行環境を用意してください。  
    - https://www.oracle.com/java/technologies/downloads/  
2. ビルドします。(2回目以降は不要)  
    - `./gradlew build` 
    - windowsのコマンドプロンプトの場合は上記コマンド頭の`./`を外してください
3. 生成されたjarファイルにて以下の要領でプログラムを実行します。
    - `java -jar ./build/libs/ts_solver.jar -t 1 31xfz11yb12yh xe1ya18b3c4d4e3f1g2`
    - 実行時の引数フォーマットの詳細は後述の項をご参照ください

# 引数のフォーマット

- 第1引数 : 手数 (〇手詰み)

- 第2引数 : 盤面  
座標 + プレイヤ種別 + 駒種別 (+成駒の場合はフラグを追加) (駒の数分繰り返し)

- 第3引数 : 持ち駒 ※省略可  
プレイヤ種別 + 駒種別 + 枚数 (種類数分繰り返し) ... + プレイヤ種別 + 駒種別 + 枚数 (種類数分繰り返し)

- ※上記に割り振る文字
  - プレイヤ種別
    - 先手 : `x`
    - 後手 : `y`
  - 座標
    - 通常の棋譜読み通り、筋/段を数字にする。(e.g.) `一三` -> `13`
  - 駒種別
    - 歩 : `a`	
    - 香 : `b`
    - 桂 : `c`
    - 銀 : `d`
    - 金 : `e`
    - 角 : `f`
    - 飛 : `g`
    - 王/玉 : `h`
  - 成駒フラグ
    - `z`
  
- 例）  
  第1引数 : `1`  
  第2引数 : `31xfz11yb12yh`  
  第3引数 : `xe1ya18b3c4d4e3f1g2`  

## オプション

|キー|詳細|
|:---|:---|
|-t|[出力フォーマット](#出力内容)を全角日本語の棋譜表記にします。|

# 出力内容

## インデックス表記(デフォルト)

以下の要領で、半角英数字で出力します。  
組み込み利用等による機械的な読み取りを想定した出力となります。

```
x-23-d---v
y2423je----
x3241-d---t
y2211-h----
x2112-d---u
y1312jg----
x3121-e----
y1121jh----
x4231-gz---
```

フォーマットの詳細は以下の通りです。`x3241jd-m-t`を例に挙げます。  
- **空の値は必ず各項「`-`」で表されます。**
- 詰めない場合は何も出力しません。

|*1|*2|*3|*4|*5|*6|*7|*8|*9|
|:---|:---|:---|:---|:---|:---|:---|:---|:---|
|x|32|41|j|d|-|m|-|t|
|先手|||同|銀||右||打|

- ***1** プレイヤ種別
  - 先手 : `x`
  - 後手 : `y`
- ***2** 移動元の座標
  - 通常の棋譜読み通り、筋/段を数字にする。
  - 持ち駒からの場合は「`-`」
- ***3** 移動先の座標
  - 通常の棋譜読み通り、筋/段を数字にする。
- ***4** 「同」フラグ
  - `j`
- ***5** 駒種別
  - 歩 : `a`	
  - 香 : `b`
  - 桂 : `c`
  - 銀 : `d`
  - 金 : `e`
  - 角 : `f`
  - 飛 : `g`
  - 王/玉 : `h`
- ***6** 成駒フラグ
  - `z`
  - この手で成る場合は成駒フラグは立たない。
    - 「`53成銀`」は成駒フラグが立つが、「`53銀成`」は立たない。
    - この場合、代わりに`*9`の成フラグで表される。
- ***7** 相対位置
  - 右 : `m`
  - 左 : `n`
- ***8** 動作
  - 上 : `p`
  - 寄 : `q`
  - 引 : `r`
  - 直 : `s`
- ***9** その他
  - 成 : `t` 
  - 不成 : `u` 
  - 打 : `v` 


## 全角棋譜表記(`-t`指定)

以下の要領で、全角の日本語で出力します。  
```
先手：２３銀打
後手：同金
先手：４１銀成
後手：１１玉
先手：１２銀不成
後手：同飛
先手：２１金
後手：同玉
先手：３１龍
```
- 棋譜表記は[日本将棋連盟の記事](https://www.shogi.or.jp/faq/kihuhyouki.html)を参考にしています。
- 文字符号化方式に関して
  - 実行環境(OS等)に合わせて文字符号化方式が決定されるため、基本的に文字化けしない想定ですが、  
  実行時に以下の要領で文字符号化方式の指定が可能です。  
  `java -Dfile.encoding=UTF-8 -jar ts_solver.jar 1 31xfz11yb12yh xe1ya18b3c4d4e3f1g2`
- 詰めない場合は「`詰めませんでした。`」と出力します。

# 補足

## 基本的なこと

- 複数回答がある場合も単一の回答を返します。  

- 指定手数よりも短い手数で詰み得る場合、より短い手数の回答を返します。  

- もし受け手の打ち筋に期待するものがある場合は、期待する手を入力し、  
詰み手数をその分減らした上で計算してください。

- 現状、攻め手の王は考慮されません。  

## パフォーマンス

- マシンスペック等にもよりますが、複雑な問題には相応に計算時間を要します。  
プログラムを実行してから数秒レスポンスが無くともエラーではない可能性が高いです。

- 難度相応にメモリ容量も要するため、メモリが不足している場合はOOMで落ちる可能性があります。  
アプリの処理に割り当てるメモリ容量はjavaコマンド実行時の引数等で指定が可能です。(`-Xmx`など)  
詳しくはは[Javaのドキュメント](https://docs.oracle.com/en/java/javase/15/docs/specs/man/java.html#extra-options-for-java)をご確認ください。

- 参考程度に、手元の状況を記します。随時更新します。
  - 条件
    - ヒープサイズ : 4G
    - プロセッサ : intel core i7-8650U
  - 指標
    - ◎ : 最遅でも10秒以内
    - 〇 : 最遅でも1分以内
    - △ : 難度次第では1分を大幅に超過してしまう
    - ？ : 未検証
  - [v2.0.0](https://github.com/begyyal/tumeshogi_solver/releases/tag/v1) 時点
    - 7手詰め迄 : ◎
    - 9手詰め : ◎
    - 11手詰め以降 : ？
  - 最遅問題
    - 7手詰め
      - `7 55xf43xd21xaz24yh13yc xg1e1ya17b4c3d3e3f1g1`
    - 9手詰め
      - `9 14yb24yc34yc44yc54yc15yh66xg xg1e4ya18b3d4f2`
