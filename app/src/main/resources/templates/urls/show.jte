@param hexlet.code.model.Url url
@param java.util.List<hexlet.code.model.UrlCheck> checks
@param String flash
@param String flashType

@template.layout(content = @`
    <h1 class="mb-6 text-3xl font-light">Сайт: ${url.getName()}</h1>
    <table data-test="url" class="mb-6 w-full border-collapse text-left">
        <tbody>
            <tr class="border-b border-gray-200">
                <td class="py-2 pr-4 font-medium">ID</td>
                <td class="py-2 pr-4">${url.getId()}</td>
            </tr>
            <tr class="border-b border-gray-200">
                <td class="py-2 pr-4 font-medium">Имя</td>
                <td class="py-2 pr-4">${url.getName()}</td>
            </tr>
            <tr class="border-b border-gray-200">
                <td class="py-2 pr-4 font-medium">Дата создания</td>
                <td class="py-2 pr-4">${url.getCreatedAt().toLocalDateTime().toLocalDate().toString()}</td>
            </tr>
        </tbody>
    </table>

    <form method="post" action="/urls/${url.getId()}/checks" class="mb-8">
        <input
            type="submit"
            value="Запустить проверку"
            class="cursor-pointer rounded bg-blue-600 px-6 py-2 text-lg font-medium text-white hover:bg-blue-700"
        />
    </form>

    <table data-test="checks" class="w-full border-collapse text-left">
        <thead>
            <tr class="border-b border-gray-300">
                <th class="py-2 pr-4">ID</th>
                <th class="py-2 pr-4">Код ответа</th>
                <th class="py-2 pr-4">h1</th>
                <th class="py-2 pr-4">title</th>
                <th class="py-2 pr-4">description</th>
                <th class="py-2 pr-4">Дата создания</th>
            </tr>
        </thead>
        <tbody>
            @for (var check : checks)
                <tr class="border-b border-gray-200">
                    <td class="py-2 pr-4">${check.getId()}</td>
                    <td class="py-2 pr-4">${check.getStatusCode()}</td>
                    <td class="py-2 pr-4">${hexlet.code.controller.UrlCheckController.truncate(check.getH1())}</td>
                    <td class="py-2 pr-4">${hexlet.code.controller.UrlCheckController.truncate(check.getTitle())}</td>
                    <td class="py-2 pr-4">${hexlet.code.controller.UrlCheckController.truncate(check.getDescription())}</td>
                    <td class="py-2 pr-4">${check.getCreatedAt().toLocalDateTime().toLocalDate().toString()}</td>
                </tr>
            @endfor
        </tbody>
    </table>
`, flash = flash, flashType = flashType)
