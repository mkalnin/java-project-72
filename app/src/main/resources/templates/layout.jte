@param gg.jte.Content content
@param String flash
@param String flashType

<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Анализатор страниц</title>
    <link href="/css/main.css" rel="stylesheet" />
</head>
<body class="min-h-screen bg-white text-gray-900">
    <nav class="border-b border-gray-200 bg-white">
        <div class="mx-auto flex max-w-3xl items-center gap-6 px-4 py-3">
            <a href="/" class="text-lg font-semibold">Анализатор страниц</a>
            <a href="/urls" class="text-gray-600 hover:text-gray-900">Сайты</a>
        </div>
    </nav>
    <main class="mx-auto max-w-3xl px-4 py-8">
        @if (flash != null && !flash.isBlank())
            @if ("error".equals(flashType))
                <div role="alert" class="mb-6 rounded border border-red-200 bg-red-50 px-4 py-3 text-red-800">
                    ${flash}
                </div>
            @elseif ("info".equals(flashType))
                <div role="alert" class="mb-6 rounded border border-yellow-200 bg-yellow-50 px-4 py-3 text-yellow-800">
                    ${flash}
                </div>
            @else
                <div role="alert" class="mb-6 rounded border border-green-200 bg-green-50 px-4 py-3 text-green-800">
                    ${flash}
                </div>
            @endif
        @endif
        ${content}
    </main>
</body>
</html>
