# UDP 基础知识

UDP 是User Datagram Protocol 的简称， 翻译为用户数据报协议。
UDP 是一种无连接的传输协议，应用程序无需创建连接就可以发送数据报。
UDP 有三种通讯方式：单播、组播、广播。
1. 通讯方式
   单播 通过指定通讯主机 IP 和端口， 可以实现将消息发送到指定主机。
   组播 数据收发仅在指定分组中进行，其他未加入分组的主机不能收发对应的数据。
   广播 将消息发送到同一广播网络中每个主机。

2. UDP 地址 

    - UDP 采用的也是类似于 IP 一样的地址，无需在操作系统中设置。只需要在应用程序中使用，且与 网卡 IP 地址不冲突。 

    - UDP 使用的广播地址为：255.255.255.255， 注意：本地广播信息不会被路由器转发。

    - 组播地址

      D 类地址用于组播，D 类地址范围为 224.0.0.0 ~ 239.255.255.255，这些地址又划分为以下 4 类

      | 地址                       | 说明                                                         |
      | -------------------------- | ------------------------------------------------------------ |
      | 224.0.0.0～224.0.0.255     | 为预留的组播地址（永久组地址），地址224.0.0.0保留不做分配，其它地址供路由协议使用 |
      | 224.0.1.0～224.0.1.255     | 是公用组播地址，可以用于 Internet；欲使用需申请              |
      | 224.0.2.0～238.255.255.255 | 为用户可用的组播地址（临时组地址），全网范围内有效           |
      | 239.0.0.0～239.255.255.255 | 为本地管理组播地址，仅在特定的本地范围内有效                 |

3. UDP数据报

    包含报头在内的数据报的最大长度为 64 K， 一些实际应用往往会限制数据报的大小，有时会降低到 8192 字节。UDP 信息包的标题很短，只有 8 个字节，相对于 TCP 的 20 个字节信息包而言，UDP 的额外开销很小

    UDP 报文没有可靠性保证、顺序保证和流量控制字段等，可靠性较差。

> 注意：UDP不存在半包和粘包的问题
>
> - TCP协议面向字节流
>
>   发送端可以是一K一K地发送数据，而接收端的应用程序可以两K两K地提走数据，当然也有可能一次提走3K或6K数据，或者一次只提走几个字节的数据，也就是说，应用程序所看到的数据是一个整体，或说是一个流（stream），一条消息有多少字节对应用程序是不可见的，因此TCP协议是面向流的协议，这也是容易出现粘包问题的原因。
>
> - UDP面向数据报
>
>   而UDP是面向数据报的协议，每个UDP段都是一条数据报，应用程序必须以数据报为单位提取数据，不能一次提取任意字节的数据，这一点和TCP是很不同的。
>
>   