# Built-in icon sets

luminegram can replace app icons with two built-in sets. The vector drawables in
`TMessagesProj/src/main/res/drawable/lumine_solar_*.xml` and `lumine_remix_*.xml`
come from these projects:

- Solar Icon Set by 480 Design, licensed CC BY 4.0
  (https://github.com/480-Design/Solar-Icon-Set).
- Remix Icon by Remix Design, licensed Apache License 2.0
  (https://github.com/Remix-Design/RemixIcon).

The Android vector conversion of both sets and the list of which stock icon each
one replaces (`TMessagesProj/src/main/assets/lumine/icon_sets.txt`) are taken from
exteraless/exteraless at c83765c (`BaseIconPacks` and the `*_solar` and `*_remix`
drawables), GPL-3.0. Only the entries whose stock icon exists in this tree are kept,
and the drawables were renamed with the `lumine_` prefix.
