@param java.util.List<hexlet.code.model.Url> urls
@param java.util.Map<Long, String> lastCheckDates
@param java.util.Map<Long, String> lastCheckStatuses
@param String flash
@param String flashType

@template.layout(content = @`
    <h1 class="mb-6 text-3xl font-light">Сайты</h1>
    <table data-test="urls" class="w-full border-collapse text-left">
        <thead>
            <tr class="border-b border-gray-300">
                <th class="py-2 pr-4">ID</th>
                <th class="py-2 pr-4">Имя</th>
                <th class="py-2 pr-4">Последняя проверка</th>
                <th class="py-2 pr-4">Код ответа</th>
                <th class="py-2 pr-4">Дата создания</th>
            </tr>
        </thead>
        <tbody>
            @for (var url : urls)
                <tr class="border-b border-gray-200">
                    <td class="py-2 pr-4">${url.getId()}</td>
                    <td class="py-2 pr-4"><a href="/urls/${url.getId()}" class="text-blue-600 hover:underline">${url.getName()}</a></td>
                    <td class="py-2 pr-4">${lastCheckDates.getOrDefault(url.getId(), "")}</td>
                    <td class="py-2 pr-4">${lastCheckStatuses.getOrDefault(url.getId(), "")}</td>
                    <td class="py-2 pr-4">${url.getCreatedAt().toLocalDateTime().toLocalDate().toString()}</td>
                </tr>
            @endfor
        </tbody>
    </table>
`, flash = flash, flashType = flashType)
