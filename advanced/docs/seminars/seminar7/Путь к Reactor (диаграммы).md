# Путь к Reactor (диаграммы)

---

## 1. Блокирующий клиент

```mermaid
sequenceDiagram
    participant T as Поток клиента
    participant S as Моковый сервер

    T->>S: GET /v1/instruments
    Note over T: поток заблокирован —<br/>ничего другого не делает,<br/>пока не придёт ответ
    S-->>T: 200 OK (JSON)
```

## 2. Масштаб проблемы

```mermaid
flowchart LR
    subgraph Pool["Пул потоков (размер N)"]
        T1["Поток 1 — занят"]
        T2["Поток 2 — занят"]
        T3["Поток 3 — занят"]
    end

    R1["Запрос 1"] --> T1 --> S["Моковый сервер"]
    R2["Запрос 2"] --> T2 --> S
    R3["Запрос 3"] --> T3 --> S
    R4["Запрос 4"] -. ждёт свободный поток .-> Pool
    R5["Запрос 5"] -. ждёт свободный поток .-> Pool
```

---

## 3. Наивные решения и их потолок

```mermaid
flowchart TB
    subgraph A["Вариант А — больше потоков"]
        direction LR
        A1["Пул побольше"] --> A2["Граница отодвинута,<br/>но не исчезла"]
    end

    subgraph B["Вариант Б — асинхронность / CompletableFuture"]
        direction LR
        B1["Поток освобождён<br/>от явного ожидания"] --> B2["Но кто-то всё равно должен<br/>узнать, что сокет готов — кто?"]
    end
```

---

## 4. ОС умеет ждать на нескольких сокетах разом

```mermaid
flowchart LR
    subgraph Process["Процесс (один поток)"]
        Th["Поток"]
    end

    subgraph OS["Операционная система"]
        Sel["select / epoll / kqueue —<br/>один вызов на все сокеты сразу"]
    end

    Th -->|один вызов| Sel
    Sel --> Sock1["Сокет 1"]
    Sel --> Sock2["Сокет 2"]
    Sel --> Sock3["Сокет 3"]
```

---

## 5. Reactor в сборе

```mermaid
flowchart LR
    New["Новый запрос"] -->|selector.wakeup| Loop
    Loop["Event loop<br/>(один поток)"] -->|selector.select| Selector["java.nio.channels.Selector"]
    Selector --> Ch1["SocketChannel 1<br/>(неблокирующий)"]
    Selector --> Ch2["SocketChannel 2<br/>(неблокирующий)"]
    Selector --> Ch3["SocketChannel 3<br/>(неблокирующий)"]
    Selector -->|готовые события:<br/>OP_CONNECT/OP_WRITE/OP_READ| Dispatch["Диспетчеризация обработчикам"]
    Dispatch --> Loop
```

| Кадр | Что нового |
|---|---|
| 1 | Один поток, один запрос, заблокирован |
| 2 | N потоков, запросы сверх N — в очереди |
| 3 | Больше потоков / async — потолок или нерешённый вопрос |
| 4 | ОС: один вызов, много сокетов |
| 5 | То же самое в терминах Java: `Selector` + неблокирующие `SocketChannel` |
