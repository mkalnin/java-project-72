@param hexlet.code.model.Url url
@param String flash
@param String flashType

@template.layout(content = @`
    <h1 class="mb-6 text-3xl font-light">Сайт: ${url.getName()}</h1>
    <table data-test="url" class="w-full border-collapse text-left">
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
`, flash = flash, flashType = flashType)
