# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# This file is the source Rails uses to define your schema when running `rails
# db:schema:load`. When creating a new database, `rails db:schema:load` tends to
# be faster and is potentially less error prone than running all of your
# migrations from scratch. Old migrations may fail to apply correctly if those
# migrations use external dependencies or application code.
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema.define(version: 2020_04_26_005828) do

  # These are extensions that must be enabled in order to support this database
  enable_extension "plpgsql"

  create_table "blacklisted_tokens", force: :cascade do |t|
    t.string "token"
    t.bigint "user_id", null: false
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
    t.integer "expire_at"
    t.index ["user_id"], name: "index_blacklisted_tokens_on_user_id"
  end

  create_table "users", force: :cascade do |t|
    t.string "email"
    t.string "password_digest"
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
    t.string "name"
    t.string "surname"
    t.integer "age"
    t.decimal "weight"
    t.boolean "email_confirmed", default: false
    t.string "confirm_token"
    t.string "img", default: "R.drawable.com_facebook_profile_picture_blank_portrait"
    t.string "temp_pass"
    t.boolean "reset_pass", default: false
    t.boolean "info_completed", default: false
    t.string "gender"
    t.boolean "oauth", default: false
    t.string "id_oauth"
  end

  add_foreign_key "blacklisted_tokens", "users"
end
