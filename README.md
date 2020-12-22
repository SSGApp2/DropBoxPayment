# DropBoxPayment

## Setup Dependencies

 >
 >* ทำก่อนพัฒนาฯ
 >

Install `2c2pPKCS7` lib [ดาวโหลด dependency นี้](https://drive.google.com/file/d/1IEAHaEhH1wVr1uTJlGuRrf2EjyT2AGkR/view?usp=sharing) to used as maven dependency

Modify following command. change `<path-to>` before execute

```
mvn install:install-file -DgroupId=th.co.123 -DartifactId=my2c2pPKCS7 -Dversion=1.0 -Dfile=<path-to>/my2c2pPKCS7.jar -Dpackaging=jar -DgeneratePom=true -DlocalRepositoryPath=libs -DcreateChecksum=true
```

This will create maven repository structure under `libs` folder

```
libs/
   th/co/123/my2c2oPKCS7/...
```

ปลด comment these codes in `pom.xml` ถ้ายังอยู่ใน comment 

```
<dependency>
    <groupId>th.co.123</groupId>
    <artifactId>my2c2pPKCS7</artifactId>
    <version>1.0</version>
</dependency>
```

```
<repositories>
    <repository>
        <id>ext-jars</id>
        <name>jars not in maven central</name>
        <url>file:///${project.basedir}/libs</url>
    </repository>
</repositories>
```
