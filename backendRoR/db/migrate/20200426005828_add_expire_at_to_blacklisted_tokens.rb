class AddExpireAtToBlacklistedTokens < ActiveRecord::Migration[6.0]
  def change
    add_column :blacklisted_tokens, :expire_at, :integer
  end
end
