- **品質担保**  
[![Java CI with Gradle](https://github.com/begyyal/tumeshogi_resolver/actions/workflows/ci.yml/badge.svg)](https://github.com/begyyal/tumeshogi_resolver/actions/workflows/ci.yml)
- **開発補助**  
[![Post processing of the PR](https://github.com/begyyal/tumeshogi_resolver/actions/workflows/closed-pr.yml/badge.svg)](https://github.com/begyyal/tumeshogi_resolver/actions/workflows/closed-pr.yml)  
[![Update process of the PR](https://github.com/begyyal/tumeshogi_resolver/actions/workflows/sync-pr.yml/badge.svg)](https://github.com/begyyal/tumeshogi_resolver/actions/workflows/sync-pr.yml)  
[![Create feature branch](https://github.com/begyyal/tumeshogi_resolver/actions/workflows/create-feature.yml/badge.svg)](https://github.com/begyyal/tumeshogi_resolver/actions/workflows/create-feature.yml)  
[![Delete feature branch](https://github.com/begyyal/tumeshogi_resolver/actions/workflows/delete-feature.yml/badge.svg)](https://github.com/begyyal/tumeshogi_resolver/actions/workflows/delete-feature.yml)  

# 概要

詰将棋を解くプログラムです。  
盤面及び持ち駒のデータを引数として渡すことで、  
詰み筋が存在する場合はその手筋を、そうでない場合は詰めない旨を標準出力します。  

# 導入

1. Java15以上の実行環境を用意してください。  
    - https://www.oracle.com/java/technologies/downloads/  
2. ビルドします。(2回目以降は不要)  
    - `./gradlew build` 
    - windowsのコマンドプロンプトの場合は上記コマンド頭の`./`を外してください
3. 生成されたjarファイルにて以下の要領でプログラムを実行します。
    - `java -jar ./build/libs/TumeshogiResolver.jar 1 31xfz11yb12yh xe1ya18b3c4d4e3f1g2`
    - 実行時の引数フォーマットの詳細は後述の項をご参照ください

# 引数のフォーマット

- 第1引数 : 手数 (〇手詰み)

- 第2引数 : 盤面  
座標 + プレイヤ種別 + 駒種別 (+成りの場合はフラグを追加) (駒の数分繰り返し)

- 第3引数 : 持ち駒 ※オプション。無し可。  
プレイヤ種別 + 駒種別 + 枚数 (種類数分繰り返し) ... + プレイヤ種別 + 駒種別 + 枚数 (種類数分繰り返し)

- 第3引数以降 : デバッグ用コンソール出力フラグ  
`debug` の文字列

- ※上記に割り振る文字
  - プレイヤ種別
    - 攻め手 : x
    - 受け手 : y
  - 座標
    - 通常の棋譜読み通り、筋/段を数字にする。(e.g.) 一三 -> 13
  - 駒種別
    - 歩 : a	
    - 香 : b
    - 桂 : c
    - 銀 : d
    - 金 : e
    - 角 : f
    - 飛 : g
    - 王 : h
  - 成りフラグ : z
  
- 例
  - その1 (1手詰め)  
    第1引数 : 1  
    第2引数 : 31xfz11yb12yh  
    第3引数 : xe1ya18b3c4d4e3f1g2  
  - その2 (5手詰め)  
    第1引数 : 5  
    第2引数 : 33xgz23xd32ya13yh15yfz  
    第3引数 : xc1ya17b4c3d3e4f1g1  
    
