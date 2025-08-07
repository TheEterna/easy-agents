```java

    private Class<IN> resolveInType() {
        // 获取当前类的父类（即CodeNode<IN, OUT>）
        Type genericSuperclass = getClass().getGenericSuperclass();

        // 检查父类是否为参数化类型（如CodeNode<String, Integer>）
        if (!(genericSuperclass instanceof ParameterizedType)) {
            throw new IllegalStateException("CodeNode必须指定具体的泛型参数（如CodeNode<String, Integer>），不能使用原始类型");
        }

        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        // 获取泛型参数（第0个是IN，第1个是OUT）
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length < 2) {
            throw new IllegalStateException("泛型参数不完整，需要指定IN和OUT类型");
        }

        Type inType = actualTypeArguments[0];
        // 检查IN是否为具体类型（不能是通配符?或类型变量T）
        if (!(inType instanceof Class)) {
            throw new IllegalStateException("IN必须是具体类型（如String），不能是通配符或未指定的类型变量");
        }

        return (Class<IN>) inType;
    }

    @SuppressWarnings("unchecked")
    private Class<OUT> resolveOutType() {
        // 获取当前类的父类（即CodeNode<IN, OUT>）
        Type genericSuperclass = getClass().getGenericSuperclass();

        // 检查父类是否为参数化类型（如CodeNode<String, Integer>）
        if (!(genericSuperclass instanceof ParameterizedType)) {
            throw new IllegalStateException("CodeNode必须指定具体的泛型参数（如CodeNode<String, Integer>），不能使用原始类型");
        }

        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        // 获取泛型参数（第0个是IN，第1个是OUT）
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length < 2) {
            throw new IllegalStateException("泛型参数不完整，需要指定IN和OUT类型");
        }

        Type inType = actualTypeArguments[1];
        // 检查IN是否为具体类型（不能是通配符?或类型变量T）
        if (!(inType instanceof Class)) {
            throw new IllegalStateException("IN必须是具体类型（如String），不能是通配符或未指定的类型变量");
        }

        return (Class<OUT>) inType;
    }
```