#!/usr/bin/env python3
import gzip
import os
import struct


DATA_VERSION = 3465
BASE_DIR = os.path.join(
    os.path.dirname(os.path.dirname(__file__)),
    "src",
    "main",
    "resources",
    "data",
    "victorymod",
    "structures",
)


class Structure:
    def __init__(self, size):
        self.size = size
        self.blocks = {}

    def in_bounds(self, x, y, z):
        sx, sy, sz = self.size
        return 0 <= x < sx and 0 <= y < sy and 0 <= z < sz

    def set(self, x, y, z, state):
        if self.in_bounds(x, y, z):
            self.blocks[(x, y, z)] = state

    def fill(self, x1, y1, z1, x2, y2, z2, state):
        for x in range(min(x1, x2), max(x1, x2) + 1):
            for y in range(min(y1, y2), max(y1, y2) + 1):
                for z in range(min(z1, z2), max(z1, z2) + 1):
                    self.set(x, y, z, state)

    def carve(self, x1, y1, z1, x2, y2, z2):
        self.fill(x1, y1, z1, x2, y2, z2, AIR)

    def line_x(self, x1, x2, y, z, state):
        for x in range(min(x1, x2), max(x1, x2) + 1):
            self.set(x, y, z, state)

    def line_y(self, x, y1, y2, z, state):
        for y in range(min(y1, y2), max(y1, y2) + 1):
            self.set(x, y, z, state)

    def line_z(self, x, y, z1, z2, state):
        for z in range(min(z1, z2), max(z1, z2) + 1):
            self.set(x, y, z, state)

    def hollow_box(self, x1, y1, z1, x2, y2, z2, wall, inner=None):
        if inner is None:
            inner = AIR
        self.fill(x1, y1, z1, x2, y2, z2, wall)
        if x2 - x1 >= 2 and y2 - y1 >= 2 and z2 - z1 >= 2:
            self.fill(x1 + 1, y1 + 1, z1 + 1, x2 - 1, y2 - 1, z2 - 1, inner)

    def ring(self, x1, z1, x2, z2, y, state):
        self.line_x(x1, x2, y, z1, state)
        self.line_x(x1, x2, y, z2, state)
        self.line_z(x1, y, z1, z2, state)
        self.line_z(x2, y, z1, z2, state)

    def plus_room(self, cx, cz, y1, y2, arm, width, state):
        self.fill(cx - width, y1, cz - arm, cx + width, y2, cz + arm, state)
        self.fill(cx - arm, y1, cz - width, cx + arm, y2, cz + width, state)

    def disk(self, cx, cy, cz, radius, state):
        r2 = radius * radius
        for x in range(cx - radius, cx + radius + 1):
            for z in range(cz - radius, cz + radius + 1):
                if (x - cx) * (x - cx) + (z - cz) * (z - cz) <= r2:
                    self.set(x, cy, z, state)

    def cylinder_shell(self, cx, cz, y1, y2, radius, state):
        outer = radius * radius
        inner = (radius - 1) * (radius - 1)
        for x in range(cx - radius, cx + radius + 1):
            for z in range(cz - radius, cz + radius + 1):
                d = (x - cx) * (x - cx) + (z - cz) * (z - cz)
                if inner < d <= outer:
                    for y in range(y1, y2 + 1):
                        self.set(x, y, z, state)

    def pyramid(self, x1, z1, x2, z2, y_start, steps, state):
        for i in range(steps):
            self.fill(x1 + i, y_start + i, z1 + i, x2 - i, y_start + i, z2 - i, state)

    def place_loot(self, chest_pos, spawner_pos, accent):
        cx, cy, cz = chest_pos
        sx, sy, sz = spawner_pos
        self.set(cx, cy, cz, CHEST)
        self.set(sx, sy, sz, SPAWNER)
        for dx in (-1, 1):
            self.set(cx + dx, cy, cz, accent)
        for dz in (-1, 1):
            self.set(sx, sy, sz + dz, accent)

    def to_nbt(self):
        palette = []
        palette_index = {}
        blocks = []
        sx, sy, sz = self.size
        for y in range(sy):
            for x in range(sx):
                for z in range(sz):
                    state = self.blocks.get((x, y, z), AIR)
                    key = state_key(state)
                    idx = palette_index.get(key)
                    if idx is None:
                        idx = len(palette)
                        palette_index[key] = idx
                        palette.append(state)
                    blocks.append({"pos": [x, y, z], "state": idx})
        return {
            "size": [sx, sy, sz],
            "entities": [],
            "blocks": blocks,
            "palette": palette,
            "DataVersion": DATA_VERSION,
        }


AIR = {"Name": "minecraft:air"}
CHEST = {"Name": "minecraft:chest"}
SPAWNER = {"Name": "minecraft:spawner"}
BEDROCK = {"Name": "minecraft:bedrock"}
OBSIDIAN = {"Name": "minecraft:obsidian"}
GLOWSTONE = {"Name": "minecraft:glowstone"}
SEA_LANTERN = {"Name": "minecraft:sea_lantern"}
SHROOMLIGHT = {"Name": "minecraft:shroomlight"}
AMETHYST = {"Name": "minecraft:amethyst_block"}
CALCITE = {"Name": "minecraft:calcite"}
TUFF = {"Name": "minecraft:tuff"}
STONE_BRICKS = {"Name": "minecraft:stone_bricks"}
MOSSY_BRICKS = {"Name": "minecraft:mossy_stone_bricks"}
CRACKED_BRICKS = {"Name": "minecraft:cracked_stone_bricks"}
DEEPSLATE = {"Name": "minecraft:polished_deepslate"}
BLACKSTONE = {"Name": "minecraft:polished_blackstone_bricks"}
PRISMARINE = {"Name": "minecraft:prismarine_bricks"}


def block(name):
    return {"Name": f"minecraft:{name}"}


def color_block(color, suffix):
    return block(f"{color}_{suffix}")


def state_key(state):
    properties = state.get("Properties", {})
    return state["Name"], tuple(sorted(properties.items()))


def write_string(value):
    encoded = value.encode("utf-8")
    return struct.pack(">h", len(encoded)) + encoded


def payload_for_value(value):
    if isinstance(value, dict):
        return 10, write_compound_payload(value)
    if isinstance(value, str):
        return 8, write_string(value)
    if isinstance(value, int):
        return 3, struct.pack(">i", value)
    if isinstance(value, list):
        if value and all(isinstance(v, int) for v in value):
            return 11, struct.pack(">i", len(value)) + b"".join(struct.pack(">i", v) for v in value)
        if value:
            tag_type, _ = payload_for_value(value[0])
        else:
            tag_type = 10
        payload = struct.pack(">bi", tag_type, len(value))
        for item in value:
            item_type, item_payload = payload_for_value(item)
            if item_type != tag_type:
                raise ValueError("Mixed list types are not supported")
            payload += item_payload
        return 9, payload
    raise TypeError(f"Unsupported value: {value!r}")


def write_named_tag(name, value):
    tag_type, payload = payload_for_value(value)
    return bytes([tag_type]) + write_string(name) + payload


def write_compound_payload(value):
    out = bytearray()
    for key, inner in value.items():
        out.extend(write_named_tag(key, inner))
    out.append(0)
    return bytes(out)


def write_structure_file(path, structure):
    root = structure.to_nbt()
    payload = bytes([10]) + write_string("") + write_compound_payload(root)
    with gzip.open(path, "wb") as fh:
        fh.write(payload)


def frame_foundation(s, x1, z1, x2, z2, floor, wall, y=0):
    s.fill(x1, y, z1, x2, y, z2, floor)
    s.ring(x1, z1, x2, z2, y + 1, wall)


def build_white():
    s = Structure((23, 18, 23))
    white = color_block("white", "wool")
    quartz = block("quartz_block")
    ice = block("packed_ice")
    glass = block("white_stained_glass")
    cx = cz = 11
    frame_foundation(s, 3, 3, 19, 19, quartz, BEDROCK)
    s.plus_room(cx, cz, 1, 9, 7, 2, quartz)
    s.plus_room(cx, cz, 2, 8, 5, 1, AIR)
    for x, z in ((6, 6), (16, 6), (6, 16), (16, 16)):
        s.fill(x, 1, z, x + 1, 12, z + 1, ice)
        s.fill(x, 13, z, x + 1, 14, z + 1, white)
    s.fill(9, 10, 9, 13, 10, 13, glass)
    s.fill(10, 11, 10, 12, 12, 12, AIR)
    s.fill(10, 13, 10, 12, 13, 12, SEA_LANTERN)
    s.line_y(cx, 1, 13, cz, GLOWSTONE)
    for x in range(7, 16, 4):
        s.fill(x, 4, 5, x, 7, 5, white)
        s.fill(x, 4, 17, x, 7, 17, white)
    s.carve(10, 1, 3, 12, 4, 5)
    s.place_loot((11, 1, 17), (11, 1, 9), white)
    return s


def build_magenta():
    s = Structure((25, 17, 25))
    magenta = color_block("magenta", "wool")
    glass = block("magenta_stained_glass")
    terracotta = block("magenta_glazed_terracotta")
    cx = cz = 12
    frame_foundation(s, 4, 4, 20, 20, BLACKSTONE, OBSIDIAN)
    s.fill(6, 1, 6, 18, 9, 18, BLACKSTONE)
    s.carve(8, 1, 8, 16, 8, 16)
    for y in range(1, 11):
        r = 8 if y < 6 else 6
        s.cylinder_shell(cx, cz, y, y, r, OBSIDIAN if y % 2 == 0 else BLACKSTONE)
    for x, z in ((12, 5), (19, 12), (12, 19), (5, 12)):
        s.fill(x, 1, z, x, 12, z, magenta)
    s.fill(10, 9, 10, 14, 9, 14, glass)
    s.fill(11, 10, 11, 13, 12, 13, terracotta)
    s.carve(11, 1, 4, 13, 4, 6)
    s.line_x(8, 16, 1, 12, AMETHYST)
    s.line_z(12, 1, 8, 16, AMETHYST)
    s.place_loot((12, 1, 16), (12, 1, 12), magenta)
    return s


def build_lightblue():
    s = Structure((21, 24, 21))
    lightblue = color_block("light_blue", "wool")
    glass = block("light_blue_stained_glass")
    quartz = block("smooth_quartz")
    cx = cz = 10
    frame_foundation(s, 4, 4, 16, 16, quartz, BEDROCK)
    s.cylinder_shell(cx, cz, 1, 15, 6, quartz)
    s.carve(6, 1, 6, 14, 14, 14)
    s.disk(cx, 1, cz, 4, lightblue)
    for y in range(3, 15, 3):
        s.ring(7, 7, 13, 13, y, glass)
    for x, z in ((10, 4), (16, 10), (10, 16), (4, 10)):
        s.fill(x, 1, z, x, 18, z, SEA_LANTERN)
    for y in range(16, 21):
        inset = y - 16
        s.fill(7 + inset, y, 7 + inset, 13 - inset, y, 13 - inset, glass)
    s.line_y(cx, 1, 20, cz, SHROOMLIGHT)
    s.carve(9, 1, 4, 11, 4, 5)
    s.place_loot((10, 2, 13), (10, 1, 10), lightblue)
    return s


def build_yellow():
    s = Structure((23, 16, 23))
    yellow = color_block("yellow", "wool")
    sand = block("cut_sandstone")
    gold = block("gold_block")
    frame_foundation(s, 2, 2, 20, 20, sand, BEDROCK)
    s.pyramid(3, 3, 19, 19, 1, 5, sand)
    s.pyramid(7, 7, 15, 15, 6, 3, yellow)
    s.fill(9, 9, 9, 13, 11, 13, gold)
    s.carve(10, 1, 2, 12, 4, 4)
    s.carve(8, 6, 8, 14, 10, 14)
    for x, z in ((5, 5), (17, 5), (5, 17), (17, 17)):
        s.fill(x, 1, z, x + 1, 6, z + 1, GLOWSTONE)
    s.line_x(9, 13, 8, 11, SHROOMLIGHT)
    s.line_z(11, 8, 9, 13, SHROOMLIGHT)
    s.place_loot((11, 6, 14), (11, 6, 11), yellow)
    return s


def build_lime():
    s = Structure((25, 14, 25))
    lime = color_block("lime", "wool")
    leaves = block("moss_block")
    bricks = block("mossy_stone_bricks")
    frame_foundation(s, 2, 2, 22, 22, bricks, BEDROCK)
    s.fill(3, 1, 3, 21, 6, 21, leaves)
    s.carve(4, 1, 4, 20, 5, 20)
    for x in range(4, 21, 4):
        s.fill(x, 1, 4, x, 4, 20, bricks)
    for z in range(4, 21, 4):
        s.fill(4, 1, z, 20, 4, z, bricks)
    s.carve(4, 1, 12, 7, 4, 12)
    s.carve(8, 1, 8, 8, 4, 15)
    s.carve(12, 1, 8, 15, 4, 8)
    s.carve(16, 1, 8, 16, 4, 15)
    s.carve(12, 1, 16, 19, 4, 16)
    s.fill(10, 1, 10, 14, 7, 14, lime)
    s.carve(11, 1, 11, 13, 6, 13)
    s.fill(11, 7, 11, 13, 8, 13, GLOWSTONE)
    s.place_loot((12, 1, 13), (12, 1, 12), lime)
    return s


def build_pink():
    s = Structure((23, 16, 23))
    pink = color_block("pink", "wool")
    planks = block("cherry_planks")
    logs = block("cherry_log")
    petals = block("pink_petals")
    frame_foundation(s, 4, 4, 18, 18, planks, BEDROCK)
    s.fill(5, 1, 5, 17, 1, 17, planks)
    for x, z in ((6, 6), (16, 6), (6, 16), (16, 16)):
        s.fill(x, 1, z, x, 9, z, logs)
    s.ring(6, 6, 16, 16, 10, pink)
    s.fill(8, 11, 8, 14, 12, 14, pink)
    s.carve(7, 1, 7, 15, 9, 15)
    s.fill(10, 1, 4, 12, 4, 6, AIR)
    s.line_x(8, 14, 1, 11, petals)
    s.line_z(11, 1, 8, 14, petals)
    for x in range(8, 15, 2):
        s.fill(x, 6, 6, x, 6, 16, GLOWSTONE)
    s.place_loot((11, 1, 14), (11, 1, 11), pink)
    return s


def build_lightgray():
    s = Structure((23, 18, 23))
    lightgray = color_block("light_gray", "wool")
    glass = block("light_gray_stained_glass")
    frame_foundation(s, 3, 3, 19, 19, STONE_BRICKS, BEDROCK)
    s.hollow_box(5, 1, 5, 17, 11, 17, STONE_BRICKS)
    s.carve(6, 1, 6, 16, 10, 16)
    for x, z in ((7, 7), (15, 7), (7, 15), (15, 15)):
        s.fill(x, 1, z, x, 13, z, TUFF)
    s.fill(9, 4, 5, 13, 7, 5, glass)
    s.fill(9, 4, 17, 13, 7, 17, glass)
    s.fill(5, 4, 9, 5, 7, 13, glass)
    s.fill(17, 4, 9, 17, 7, 13, glass)
    s.fill(8, 12, 8, 14, 12, 14, CRACKED_BRICKS)
    s.fill(10, 13, 10, 12, 14, 12, lightgray)
    s.carve(10, 1, 3, 12, 4, 5)
    s.place_loot((11, 1, 14), (11, 1, 11), lightgray)
    return s


def build_cyan():
    s = Structure((21, 16, 21))
    cyan = color_block("cyan", "wool")
    glass = block("cyan_stained_glass")
    prism = PRISMARINE
    frame_foundation(s, 3, 3, 17, 17, prism, BEDROCK)
    s.hollow_box(5, 1, 5, 15, 10, 15, prism)
    s.carve(6, 1, 6, 14, 9, 14)
    s.fill(8, 1, 8, 12, 1, 12, CALCITE)
    s.fill(9, 2, 9, 11, 4, 11, cyan)
    for x, z in ((10, 5), (15, 10), (10, 15), (5, 10)):
        s.fill(x, 1, z, x, 11, z, glass)
    s.fill(8, 11, 8, 12, 11, 12, SEA_LANTERN)
    s.fill(9, 12, 9, 11, 13, 11, glass)
    s.carve(9, 1, 3, 11, 4, 5)
    s.place_loot((10, 1, 13), (10, 2, 10), cyan)
    return s


def build_purple():
    s = Structure((25, 18, 25))
    purple = color_block("purple", "wool")
    purple_block = color_block("purple", "concrete")
    cx = cz = 12
    frame_foundation(s, 3, 3, 21, 21, OBSIDIAN, BEDROCK)
    s.fill(5, 1, 5, 19, 11, 19, OBSIDIAN)
    s.carve(7, 1, 7, 17, 10, 17)
    for x, z in ((7, 7), (17, 7), (7, 17), (17, 17)):
        s.fill(x, 1, z, x, 13, z, AMETHYST)
    for y in range(1, 13):
        s.ring(6, 6, 18, 18, y, OBSIDIAN if y % 2 else purple_block)
    s.line_x(8, 16, 5, cz, purple)
    s.line_z(cx, 5, 8, 16, purple)
    s.fill(10, 12, 10, 14, 13, 14, AMETHYST)
    s.carve(11, 1, 3, 13, 4, 5)
    s.place_loot((12, 1, 16), (12, 1, 12), purple)
    return s


def build_blue():
    s = Structure((21, 26, 21))
    blue = color_block("blue", "wool")
    lapis = block("lapis_block")
    glass = block("blue_stained_glass")
    cx = cz = 10
    frame_foundation(s, 4, 4, 16, 16, PRISMARINE, BEDROCK)
    s.cylinder_shell(cx, cz, 1, 20, 5, lapis)
    s.carve(7, 1, 7, 13, 19, 13)
    for y in range(3, 19, 4):
        s.ring(7, 7, 13, 13, y, glass)
    s.fill(9, 21, 9, 11, 23, 11, SEA_LANTERN)
    s.fill(8, 24, 8, 12, 24, 12, blue)
    s.fill(10, 1, 4, 10, 20, 4, GLOWSTONE)
    s.carve(9, 1, 4, 11, 4, 5)
    s.place_loot((10, 2, 12), (10, 1, 10), blue)
    return s


def build_green():
    s = Structure((23, 17, 23))
    green = color_block("green", "wool")
    jungle = block("jungle_planks")
    moss = MOSSY_BRICKS
    frame_foundation(s, 2, 2, 20, 20, moss, BEDROCK)
    s.pyramid(4, 4, 18, 18, 1, 4, moss)
    s.fill(7, 5, 7, 15, 10, 15, jungle)
    s.carve(8, 5, 8, 14, 9, 14)
    for x, z in ((8, 8), (14, 8), (8, 14), (14, 14)):
        s.fill(x, 5, z, x, 12, z, green)
    s.fill(9, 10, 9, 13, 12, 13, SHROOMLIGHT)
    s.line_x(9, 13, 5, 11, green)
    s.line_z(11, 5, 9, 13, green)
    s.carve(10, 5, 4, 12, 8, 6)
    s.place_loot((11, 5, 13), (11, 5, 11), green)
    return s


def build_black():
    s = Structure((27, 18, 27))
    black = color_block("black", "wool")
    basalt = block("basalt")
    soul = block("soul_lantern")
    frame_foundation(s, 3, 3, 23, 23, BLACKSTONE, BEDROCK)
    s.hollow_box(5, 1, 5, 21, 11, 21, BLACKSTONE)
    s.carve(6, 1, 6, 20, 10, 20)
    for x, z in ((6, 6), (20, 6), (6, 20), (20, 20)):
        s.fill(x, 1, z, x + 1, 14, z + 1, basalt)
    s.ring(7, 7, 19, 19, 12, black)
    s.fill(10, 1, 10, 16, 1, 16, OBSIDIAN)
    s.fill(11, 2, 11, 15, 4, 15, AIR)
    s.fill(12, 5, 12, 14, 6, 14, soul)
    s.line_x(10, 16, 4, 13, black)
    s.line_z(13, 4, 10, 16, black)
    s.carve(12, 1, 3, 14, 4, 5)
    s.place_loot((13, 1, 17), (13, 1, 13), black)
    return s


BUILDERS = {
    "dungeon_white.nbt": build_white,
    "dungeon_magenta.nbt": build_magenta,
    "dungeon_lightblue.nbt": build_lightblue,
    "dungeon_yellow.nbt": build_yellow,
    "dungeon_lime.nbt": build_lime,
    "dungeon_pink.nbt": build_pink,
    "dungeon_lightgray.nbt": build_lightgray,
    "dungeon_cyan.nbt": build_cyan,
    "dungeon_purple.nbt": build_purple,
    "dungeon_blue.nbt": build_blue,
    "dungeon_green.nbt": build_green,
    "dungeon_black.nbt": build_black,
}


def main():
    os.makedirs(BASE_DIR, exist_ok=True)
    for filename, builder in BUILDERS.items():
        path = os.path.join(BASE_DIR, filename)
        write_structure_file(path, builder())
        print(f"wrote {filename}")


if __name__ == "__main__":
    main()
