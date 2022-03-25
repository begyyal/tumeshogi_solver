[![Test [lib] by sync PR](https://github.com/begyyal/tumeshogi_solver/actions/workflows/sync-pr2.yml/badge.svg)](https://github.com/begyyal/tumeshogi_solver/actions/workflows/sync-pr2.yml)

# 概要

Javaライブラリ形式です。  
Mavenリポジトリのような公開はしていないため、ご利用に際してはjarファイルをご利用ください。

# 導入

1. Javaのバージョンは15以上が必要になります。  
    - https://www.oracle.com/java/technologies/downloads/  
2. jarファイルは[こちら](../cmd/jar/ts_solver.jar)になります。
    - `tumeshogi_solver/cmd/jar/ts_solver.jar`

# API

`begyyal.shogi.TsSolver#calculate()`が計算処理となります。  
簡単な[サンプル](./test/sample/sample.java)があるのでご覧ください。

## 各種Enumのキー文字

1. プレイヤ種別
    - 先手 : `x`
    - 後手 : `y`
2. 「同」フラグ
    - `j`
3. 駒種別
    - 歩 : `a`	
    - 香 : `b`
    - 桂 : `c`
    - 銀 : `d`
    - 金 : `e`
    - 角 : `f`
    - 飛 : `g`
    - 王/玉 : `h`
4. 相対位置
    - 右 : `m`
    - 左 : `n`
5. 動作
    - 上 : `p`
    - 寄 : `q`
    - 引 : `r`
    - 直 : `s`
6. その他
    - 成 : `t` 
    - 不成 : `u` 
    - 打 : `v` 
